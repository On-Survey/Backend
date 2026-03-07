package OneQ.OnSurvey.domain.participation.service.answer;

import OneQ.OnSurvey.domain.participation.entity.QuestionAnswer;
import OneQ.OnSurvey.domain.participation.entity.Response;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;
import OneQ.OnSurvey.domain.participation.repository.answer.AnswerRepository;
import OneQ.OnSurvey.domain.participation.repository.response.ResponseRepository;
import OneQ.OnSurvey.domain.survey.SurveyErrorCode;
import OneQ.OnSurvey.global.common.exception.CustomException;
import OneQ.OnSurvey.global.common.exception.ErrorCode;
import OneQ.OnSurvey.global.infra.redis.RedisAgent;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.RedisException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class QuestionAnswerCommandService extends AnswerCommandService<QuestionAnswer> {

    @Value("${redis.survey-key-prefix.lock}")
    private String surveyLockKeyPrefix;

    private final RedisAgent redisAgent;

    public QuestionAnswerCommandService(
        AnswerRepository<QuestionAnswer> answerRepository,
        ResponseRepository responseRepository,
        RedisAgent redisAgent
    ) {
        super(answerRepository, responseRepository);
        this.redisAgent = redisAgent;
    }

    @Override
    @Transactional
    public QuestionAnswer createAnswerFromDto(AnswerInsertDto.AnswerInfo answerInfo) {
        return QuestionAnswer.from(answerInfo);
    }

    @Override
    public Boolean upsertAnswers(AnswerInsertDto insertDto, Long surveyId, Long userKey, Long memberId) {
        log.info("[QUESTION_ANSWER:COMMAND] 문항 응답 생성 - memberId: {}", memberId);

        // 새로운 응답을 questionId 기준으로 그룹화
        Map<Long, Set<QuestionAnswer>> newQuestionAnswerMap = insertDto.getAnswerInfoList().stream()
            .map(this::createAnswerFromDto)
            .collect(Collectors.groupingBy(QuestionAnswer::getQuestionId, Collectors.toSet()));
        List<Long> questionIdList = newQuestionAnswerMap.keySet().stream().toList();


        String lockKey = surveyLockKeyPrefix + surveyId + ":" + userKey;
        try {
            return redisAgent.executeNewTransactionAfterLock(lockKey, 3, () -> {
                /*
                    새로운 응답의 questionId로부터 기존 응답 조회 및 그룹화
                    Phantom Read 방지를 위해 조회 로직도 락 내부에서 실행
                 */
                Map<Long, Set<QuestionAnswer>> existingQuestionAnswerMap =
                    answerRepository.getAnswerListByQuestionIdsAndMemberId(questionIdList, memberId)
                        .stream()
                        .collect(Collectors.groupingBy(QuestionAnswer::getQuestionId, Collectors.toSet()));

                // 새로 저장할 응답 리스트
                List<QuestionAnswer> finalAnswersToSave = new ArrayList<>();
                // 삭제하지 않을 ID
                Set<Long> idSetToKeep = new HashSet<>();

                questionIdList.forEach(questionId -> {
                    // questionId에 대한 새로운 응답과 기존 응답의 content 집합 생성
                    Set<QuestionAnswer> newAnswerContentSet = newQuestionAnswerMap.getOrDefault(questionId, Set.of());
                    Set<String> newContents = newAnswerContentSet.stream()
                        .map(QuestionAnswer::getContent)
                        .map(content -> content == null ? null : content.strip())
                        .collect(Collectors.toSet());
                    Set<QuestionAnswer> existingAnswerContentSet = existingQuestionAnswerMap.getOrDefault(questionId, Set.of());
                    Set<String> existingContents = existingAnswerContentSet.stream()
                        .map(QuestionAnswer::getContent)
                        .collect(Collectors.toSet());

                    // 새로운 응답이 null을 포함한 경우(객관식) 혹은 빈 문자열인 경우(단답/장문), 해당 문항의 기존 응답은 모두 삭제 대상에 남겨둠
                    if (!newContents.contains(null) && !newContents.contains("")) {
                        // 새로운 응답 중 기존에 없는 content는 저장 대상에 추가
                        newAnswerContentSet.stream()
                            .filter(newAnswer -> !existingContents.contains(newAnswer.getContent()))
                            .forEach(finalAnswersToSave::add);
                        // 새로운 응답에 포함된 기존 응답은 삭제 대상에서 제외
                        existingAnswerContentSet.stream()
                            .filter(existingAnswer -> newContents.contains(existingAnswer.getContent()))
                            .map(QuestionAnswer::getAnswerId)
                            .forEach(idSetToKeep::add);
                    }
                });
                // 삭제할 기존 응답 ID 리스트 (초기값: 기존 응답 전체)
                List<Long> finalAnswerIdsToDelete = existingQuestionAnswerMap.values().stream()
                    .flatMap(Collection::stream)
                    .map(QuestionAnswer::getAnswerId)
                    .collect(Collectors.toList());
                finalAnswerIdsToDelete.removeAll(idSetToKeep);

                if (!finalAnswersToSave.isEmpty()) {
                    answerRepository.saveAll(finalAnswersToSave);
                }
                if (!finalAnswerIdsToDelete.isEmpty()) {
                    answerRepository.deleteAllByIds(finalAnswerIdsToDelete);
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
