package OneQ.OnSurvey.domain.participation.service.answer;

import OneQ.OnSurvey.domain.participation.entity.QuestionAnswer;
import OneQ.OnSurvey.domain.participation.entity.Response;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;
import OneQ.OnSurvey.domain.participation.repository.answer.AnswerRepository;
import OneQ.OnSurvey.domain.participation.repository.response.ResponseRepository;
import OneQ.OnSurvey.domain.question.repository.question.QuestionRepository;
import OneQ.OnSurvey.domain.survey.SurveyErrorCode;
import OneQ.OnSurvey.global.common.exception.CustomException;
import OneQ.OnSurvey.global.common.exception.ErrorCode;
import OneQ.OnSurvey.global.infra.redis.RedisAgent;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.RedisException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class QuestionAnswerCommandService extends AnswerCommandService<QuestionAnswer> {

    @Value("${redis.survey-key-prefix.lock}")
    private String surveyLockKeyPrefix;

    private final RedisAgent redisAgent;
    private final QuestionRepository questionRepository;

    public QuestionAnswerCommandService(
        AnswerRepository<QuestionAnswer> answerRepository,
        ResponseRepository responseRepository,
        RedisAgent redisAgent,
        QuestionRepository questionRepository
    ) {
        super(answerRepository, responseRepository);
        this.redisAgent = redisAgent;
        this.questionRepository = questionRepository;
    }

    @Override
    @Transactional
    public QuestionAnswer createAnswerFromDto(AnswerInsertDto.AnswerInfo answerInfo) {
        return QuestionAnswer.from(answerInfo);
    }

    @Override
    public Boolean upsertAnswers(AnswerInsertDto insertDto, Long surveyId, Long userKey, Long memberId) {
        log.info("[QUESTION_ANSWER:COMMAND] 문항 응답 생성 - memberId: {}", memberId);
        String lockKey = surveyLockKeyPrefix + surveyId + ":" + userKey;
        try {
            return redisAgent.executeNewTransactionAfterLock(lockKey, 0, () -> {
                int section = insertDto.getSection();
                Set<Long> questionIdSet = new HashSet<>(questionRepository.getQuestionIdListBySurveyIdAndSection(surveyId, section));

                // 섹션에 해당하는 문항에 대한 응답 제출이 이루어졌는지 검증
                List<AnswerInsertDto.AnswerInfo> answerInfoList = insertDto.getAnswerInfoList() != null ? insertDto.getAnswerInfoList() : List.of();
                boolean hasInvalidQuestionId = answerInfoList.stream()
                    .map(AnswerInsertDto.AnswerInfo::getId)
                    .anyMatch(questionId -> questionId == null || !questionIdSet.contains(questionId));
                if (hasInvalidQuestionId) {
                    log.warn("[QUESTION_ANSWER:COMMAND] 섹션, 문항 ID 불일치 - section: {}", section);
                    throw new CustomException(SurveyErrorCode.SURVEY_ANSWER_INVALID);
                }

                // 문항이 없는 빈 섹션
                if (questionIdSet.isEmpty()) {
                    return true;
                }

                List<QuestionAnswer> finalAnswersToSave = answerInfoList.stream()
                    .filter(info -> info.getContent() != null && !info.getContent().isBlank())
                    .map(this::createAnswerFromDto)
                    .peek(answer -> answer.updateContent(answer.getContent().strip()))
                    .toList();

                answerRepository.deleteBySurveyIdAndSectionAndMemberId(surveyId, section, memberId);
                if (!finalAnswersToSave.isEmpty()) {
                    answerRepository.saveAll(finalAnswersToSave);
                }
                updateResponseAfterQuestionAnswers(surveyId, memberId);

                return true;
            });
        } catch (RedisException e) {
            log.warn("[QUESTION_ANSWER:COMMAND] 문항 응답 저장 락 획득 실패 - memberId: {}, error: {}", memberId, e.getMessage());
            throw new CustomException(SurveyErrorCode.SURVEY_PARTICIPATION_IN_PROCESS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[QUESTION_ANSWER:COMMAND] 문항 응답 저장 중 오류 발생 - memberId: {}, error: {}", memberId, e.getMessage());
            throw new CustomException(ErrorCode.SERVER_UNTRACKED_ERROR);
        }
    }

    // TODO - memberId를 userKey로 변경
    public void updateResponseAfterQuestionAnswers(
            Long surveyId, Long memberId
    ) {
        Response response = responseRepository
                .findBySurveyIdAndMemberId(surveyId, memberId)
                .orElseGet(() -> Response.of(surveyId, memberId));

        // 완료된 응답이 잘못 업데이트 되는 것을 방지하기 위해, 응답이 완료되지 않은 경우에만 저장
        if (Boolean.FALSE.equals(response.getIsResponded())) {
            responseRepository.save(response);
        }
    }
}
