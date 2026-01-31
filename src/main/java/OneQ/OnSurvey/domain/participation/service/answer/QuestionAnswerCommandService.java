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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
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
    public Boolean insertAnswers(AnswerInsertDto insertDto) {
        Long memberId = insertDto.getAnswerInfoList().getFirst().getMemberId();
        log.info("[QUESTION_ANSWER:COMMAND] 문항 응답 생성 - memberId: {}", memberId);

        Map<Long, List<QuestionAnswer>> newQuestionAnswerMap = insertDto.getAnswerInfoList().stream()
            .map(this::createAnswerFromDto)
            .collect(Collectors.groupingBy(QuestionAnswer::getQuestionId));
        List<Long> questionIdList = insertDto.getAnswerInfoList().stream().map(AnswerInsertDto.AnswerInfo::getId).toList();

        Map<Long, QuestionAnswer> existingQuestionAnswerMap =
            answerRepository.getAnswerListByQuestionIdsAndMemberId(questionIdList, memberId)
                .stream()
                .collect(Collectors.toMap(
                    QuestionAnswer::getQuestionId, Function.identity(),
                    (existing, replacement) -> existing
                ));

        List<QuestionAnswer> upsertQuestionList = questionIdList.stream()
            .flatMap(questionId -> {
                List<QuestionAnswer> newAnswerList = newQuestionAnswerMap.get(questionId);
                return newAnswerList.stream().map(newAnswer -> {
                    if (existingQuestionAnswerMap.get(questionId) != null) {
                        QuestionAnswer existing = existingQuestionAnswerMap.get(questionId);

                        if (!Objects.equals(newAnswer.getContent(), existing.getContent())) {
                            existing.updateContent(newAnswer.getContent());
                            return existing;
                        } else {
                            return null;
                        }
                    } else {
                        return newAnswer;
                    }
                });
            })
            .toList();

        answerRepository.saveAll(upsertQuestionList);

        AnswerInsertDto.AnswerInfo first = insertDto.getAnswerInfoList().getFirst();
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
