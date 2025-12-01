package OneQ.OnSurvey.domain.participation.service.answer;

import OneQ.OnSurvey.domain.participation.entity.QuestionAnswer;
import OneQ.OnSurvey.domain.participation.entity.Response;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;
import OneQ.OnSurvey.domain.participation.repository.answer.AnswerRepository;
import OneQ.OnSurvey.domain.participation.repository.response.ResponseRepository;
import OneQ.OnSurvey.domain.question.repository.question.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Boolean result = super.insertAnswers(insertDto);

        AnswerInsertDto.AnswerInfo first = insertDto.getAnswerInfoList().getFirst();
        Long surveyId = getSurveyIdFromQuestion(first.getId());

        updateResponseAfterQuestionAnswers(surveyId, first);

        return result;
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

        response.markResponded();
        responseRepository.save(response);
    }
}
