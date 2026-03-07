package OneQ.OnSurvey.domain.participation.service.response;

import OneQ.OnSurvey.domain.participation.entity.Response;
import OneQ.OnSurvey.domain.participation.repository.response.ResponseRepository;
import OneQ.OnSurvey.domain.survey.SurveyErrorCode;
import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.domain.survey.repository.SurveyRepository;
import OneQ.OnSurvey.domain.survey.repository.surveyInfo.SurveyInfoRepository;
import OneQ.OnSurvey.domain.survey.service.SurveyGlobalStatsService;
import OneQ.OnSurvey.global.common.exception.CustomException;
import OneQ.OnSurvey.global.common.exception.ErrorCode;
import OneQ.OnSurvey.global.infra.redis.RedisAgent;
import OneQ.OnSurvey.global.infra.transaction.AfterCommitExecutor;
import OneQ.OnSurvey.global.infra.transaction.AfterRollbackExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.RedisException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResponseCommandService implements ResponseCommand {

    private final ResponseRepository responseRepository;
    private final SurveyRepository surveyRepository;
    private final SurveyInfoRepository surveyInfoRepository;
    private final SurveyGlobalStatsService surveyGlobalStatsService;

    private final AfterCommitExecutor afterCommitExecutor;
    private final AfterRollbackExecutor afterRollbackExecutor;
    private final RedisAgent redisAgent;

    @Value("${redis.survey-key-prefix.lock}")
    private String surveyLockKeyPrefix;
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
        try {
            return redisAgent.executeNewTransactionAfterLock(surveyLockKeyPrefix + surveyId + ":" + userKey, 3, () -> {
                Response response = responseRepository
                    .findBySurveyIdAndMemberId(surveyId, memberId)
                    .orElseGet(() -> Response.of(surveyId, memberId));

                if (Boolean.TRUE.equals(response.getIsResponded())) {
                    throw new CustomException(SurveyErrorCode.SURVEY_ALREADY_PARTICIPATED);
                }

                response.markResponded();
                responseRepository.save(response);
                surveyGlobalStatsService.addCompletedCount(1);
                surveyInfoRepository.increaseCompletedCount(surveyId);

                Survey survey = surveyRepository.getSurveyById(surveyId)
                    .orElseThrow(() -> new CustomException(SurveyErrorCode.SURVEY_NOT_FOUND));
                if (SurveyStatus.ONGOING.equals(survey.getStatus())) {
                    int currCompleted = updateCounter(surveyId, userKey);
                    int dueCount = redisAgent.getIntValue(this.dueCountKey + surveyId);

                    if (currCompleted >= dueCount) {
                        survey.updateSurveyStatus(SurveyStatus.CLOSED);

                        afterCommitExecutor.run(() -> {
                            redisAgent.deleteKeys(List.of(
                                this.dueCountKey + surveyId,
                                this.completedKey + surveyId,
                                this.potentialKey + surveyId,
                                this.creatorKey + surveyId
                            ));
                        });
                    }

                    // DB 롤백 시 캐시 롤백을 위한 보상 트랜잭션
                    afterRollbackExecutor.run(() -> {
                        redisAgent.decrementValue(this.completedKey + surveyId);
                        redisAgent.addToZSet(this.potentialKey + surveyId, String.valueOf(userKey), System.currentTimeMillis());
                    });
                }

                return true;
            });
        } catch (RedisException e) {
            log.warn("[RESPONSE:COMMAND] 응답완료 처리 중 레디스 락 획득 실패 - surveyId: {}, userKey: {}. 잠시 후 다시 시도해주세요.", surveyId, userKey, e);
            throw new CustomException(SurveyErrorCode.SURVEY_PARTICIPATION_IN_PROCESS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[RESPONSE:COMMAND] 응답완료 처리 중 인터럽트 발생 - surveyId: {}, userKey: {}", surveyId, userKey, e);
            throw new CustomException(ErrorCode.SERVER_UNTRACKED_ERROR);
        }
    }

    private int updateCounter(Long surveyId, Long userKey) {
        // 완료 인원 추가
        Long currCompleted = redisAgent.incrementValue(this.completedKey + surveyId);
        // 잠재 응답자 Sorted Set에서 제거
        redisAgent.removeFromZSet(this.potentialKey + surveyId, String.valueOf(userKey));

        if (currCompleted == null) {
            log.error("[RESPONSE:COMMAND] 레디스 완료 값 갱신 실패 - surveyId: {}, userKey: {}", surveyId, userKey);
            throw new CustomException(ErrorCode.SERVER_UNTRACKED_ERROR);
        }
        return currCompleted.intValue();
    }
}
