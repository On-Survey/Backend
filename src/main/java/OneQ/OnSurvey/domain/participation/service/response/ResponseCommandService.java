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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ResponseCommandService implements ResponseCommand {

    private final StringRedisTemplate redisTemplate;

    private final ResponseRepository responseRepository;
    private final SurveyRepository surveyRepository;
    private final SurveyInfoRepository surveyInfoRepository;
    private final SurveyGlobalStatsService surveyGlobalStatsService;

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
        Integer currCompleted = updateCounter(surveyId, userKey);
        if (currCompleted != null && currCompleted.equals(surveyInfo.getDueCount())) {
            Survey survey = surveyRepository.getSurveyById(surveyId)
                .orElseThrow(() -> new CustomException(SurveyErrorCode.SURVEY_NOT_FOUND));

            survey.updateSurveyStatus(SurveyStatus.CLOSED);
            deleteAllRedisKeys(surveyId);
        }

        return true;
    }

    private void deleteAllRedisKeys(Long surveyId) {
        redisTemplate.delete(List.of(
            this.dueCountKey + surveyId,
            this.completedKey + surveyId,
            this.potentialKey + surveyId,
            this.creatorKey + surveyId
        ));
    }

    private Integer updateCounter(Long surveyId, Long userKey) {
        String potentialKey = this.potentialKey + surveyId;
        String completedKey = this.completedKey + surveyId;
        String memberValue = String.valueOf(userKey);

        // 완료 인원 추가
        Long currCompleted = redisTemplate.opsForValue().increment(completedKey);
        // 잠재 응답자 Sorted Set에서 제거
        redisTemplate.opsForZSet().remove(potentialKey, memberValue);
        return currCompleted != null ? currCompleted.intValue() : null;
    }
}
