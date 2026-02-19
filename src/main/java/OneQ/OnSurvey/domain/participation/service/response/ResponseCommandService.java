package OneQ.OnSurvey.domain.participation.service.response;

import OneQ.OnSurvey.domain.participation.entity.Response;
import OneQ.OnSurvey.domain.participation.repository.response.ResponseRepository;
import OneQ.OnSurvey.domain.survey.SurveyErrorCode;
import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.entity.SurveyInfo;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.domain.survey.repository.SurveyRepository;
import OneQ.OnSurvey.domain.survey.repository.surveyInfo.SurveyInfoRepository;
import OneQ.OnSurvey.domain.survey.service.SurveyGlobalStatsService;
import OneQ.OnSurvey.global.common.exception.CustomException;
import OneQ.OnSurvey.global.common.exception.ErrorCode;
import OneQ.OnSurvey.global.infra.redis.RedisAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ResponseCommandService implements ResponseCommand {

    private final ResponseRepository responseRepository;
    private final SurveyRepository surveyRepository;
    private final SurveyInfoRepository surveyInfoRepository;
    private final SurveyGlobalStatsService surveyGlobalStatsService;
    private final RedisAgent regisAgent;

    @Value("${redis.survey-key-prefix.potential-count}")
    private String potentialKey;
    @Value("${redis.survey-key-prefix.completed-count}")
    private String completedKey;
    @Value("${redis.survey-key-prefix.due-count}")
    private String dueCountKey;
    @Value("${redis.survey-key-prefix.creator-userkey}")
    private String creatorKey;

    @Override
    public Boolean createResponse(Long surveyId, Long memberId, Long userKey) {
        Response response = responseRepository
            .findBySurveyIdAndMemberId(surveyId, memberId)
            .orElseGet(() -> Response.of(surveyId, memberId));

        if (Boolean.TRUE.equals(response.getIsResponded())) {
            throw new CustomException(SurveyErrorCode.SURVEY_ALREADY_PARTICIPATED);
        }

        response.markResponded();
        responseRepository.save(response);

        surveyGlobalStatsService.addCompletedCount(1);

        SurveyInfo surveyInfo = surveyInfoRepository.findBySurveyId(surveyId)
            .orElseThrow(() -> new CustomException(SurveyErrorCode.SURVEY_INFO_NOT_FOUND));

        surveyInfo.increaseCompletedCount();
        int currCompleted = updateCounter(surveyId, userKey);
        if (currCompleted >= surveyInfo.getDueCount()) {
            Survey survey = surveyRepository.getSurveyById(surveyId)
                .orElseThrow(() -> new CustomException(SurveyErrorCode.SURVEY_NOT_FOUND));

            survey.updateSurveyStatus(SurveyStatus.CLOSED);
            regisAgent.deleteKeys(List.of(
                this.dueCountKey + surveyId,
                this.completedKey + surveyId,
                this.potentialKey + surveyId,
                this.creatorKey + surveyId
            ));
        }

        return true;
    }

    private int updateCounter(Long surveyId, Long userKey) {
        // 완료 인원 추가
        Long currCompleted = regisAgent.incrementValue(this.completedKey + surveyId);
        // 잠재 응답자 Sorted Set에서 제거
        regisAgent.removeFromZSet(this.potentialKey + surveyId, String.valueOf(userKey));

        if (currCompleted == null) {
            log.error("[RESPONSE:COMMAND] 레디스 완료 값 갱신 실패 - surveyId: {}, userKey: {}", surveyId, userKey);
            throw new CustomException(ErrorCode.SERVER_UNTRACKED_ERROR);
        }
        return currCompleted.intValue();
    }
}
