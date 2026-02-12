package OneQ.OnSurvey.domain.participation.service.answer;

import OneQ.OnSurvey.domain.participation.entity.QuestionAnswer;
import OneQ.OnSurvey.domain.participation.entity.Response;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;
import OneQ.OnSurvey.domain.participation.repository.answer.AnswerRepository;
import OneQ.OnSurvey.domain.participation.repository.response.ResponseRepository;
import OneQ.OnSurvey.domain.question.repository.question.QuestionRepository;
import lombok.extern.slf4j.Slf4j;
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
@Transactional
public class QuestionAnswerCommandService extends AnswerCommandService<QuestionAnswer> {

    private final QuestionRepository questionRepository;

    public QuestionAnswerCommandService(
        AnswerRepository<QuestionAnswer> answerRepository,
        ResponseRepository responseRepository,
        QuestionRepository questionRepository
    ) {
        super(answerRepository, responseRepository);
        this.questionRepository = questionRepository;
    }

    @Override
    public QuestionAnswer createAnswerFromDto(AnswerInsertDto.AnswerInfo answerInfo) {
        return QuestionAnswer.from(answerInfo);
    }

    @Override
    public Boolean upsertAnswers(AnswerInsertDto insertDto) {
        AnswerInsertDto.AnswerInfo first = insertDto.getAnswerInfoList().getFirst();
        Long memberId = first.getMemberId();
        log.info("[QUESTION_ANSWER:COMMAND] 문항 응답 생성 - memberId: {}", memberId);

        // 새로운 응답을 questionId 기준으로 그룹화
        Map<Long, Set<QuestionAnswer>> newQuestionAnswerMap = insertDto.getAnswerInfoList().stream()
            .map(this::createAnswerFromDto)
            .collect(Collectors.groupingBy(QuestionAnswer::getQuestionId, Collectors.toSet()));

        // 새로운 응답의 questionId로부터 기존 응답 조회 및 그룹화
        List<Long> questionIdList = newQuestionAnswerMap.keySet().stream().toList();
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

        Long surveyId = getSurveyIdFromQuestion(first.getId());
        updateResponseAfterQuestionAnswers(surveyId, first);

        return true;
    }

    private Long getSurveyIdFromQuestion(Long questionId) {
        return questionRepository.getSurveyId(questionId);
    }

    public void updateResponseAfterQuestionAnswers(
            Long surveyId,
            AnswerInsertDto.AnswerInfo answerInfo
    ) {
        Response response = responseRepository
                .findBySurveyIdAndMemberId(surveyId, answerInfo.getMemberId())
                .orElseGet(() -> Response.of(surveyId, answerInfo.getMemberId()));

        responseRepository.save(response);
    }
}
