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
    private static String potentialKey;

    @Value("${redis.survey-key-prefix.completed-count}")
    private static String completedKey;

    @Value("${redis.survey-key-prefix.due-count}")
    private static String dueCountKey;

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
        completeSurvey(surveyId, userKey);
        if (surveyInfo.getCompletedCount().equals(surveyInfo.getDueCount())) {
            Survey survey = surveyRepository.getSurveyById(surveyId)
                .orElseThrow(() -> new CustomException(SurveyErrorCode.SURVEY_NOT_FOUND));

            survey.updateSurveyStatus(SurveyStatus.CLOSED);
            redisTemplate.delete(dueCountKey + surveyId);
            redisTemplate.delete(completedKey + surveyId);
            redisTemplate.delete(potentialKey + surveyId);
        }

        return true;
    }

    private void completeSurvey(Long surveyId, Long userKey) {
        String potentialKey = ResponseCommandService.potentialKey + surveyId;
        String completedKey = ResponseCommandService.completedKey + surveyId;
        String memberValue = String.valueOf(userKey);

        // 완료 인원 추가
        redisTemplate.opsForValue().increment(completedKey);
        // 잠재 응답자 Sorted Set에서 제거
        redisTemplate.opsForZSet().remove(potentialKey, memberValue);
    }
}
