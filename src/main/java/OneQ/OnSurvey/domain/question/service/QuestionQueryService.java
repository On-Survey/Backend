package OneQ.OnSurvey.domain.question.service;

import OneQ.OnSurvey.domain.question.entity.ChoiceOption;
import OneQ.OnSurvey.domain.question.entity.Question;
import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;
import OneQ.OnSurvey.domain.question.repository.choiceOption.ChoiceOptionRepository;
import OneQ.OnSurvey.domain.question.repository.question.QuestionRepository;
import OneQ.OnSurvey.domain.survey.model.response.ParticipationQuestionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionQueryService implements QuestionQuery {
    private final QuestionRepository questionRepository;
    private final ChoiceOptionRepository choiceOptionRepository;

    @Override
    public ParticipationQuestionResponse getQuestionListBySurveyId(Long surveyId) {
        List<Question> questionList = questionRepository.getQuestionListBySurveyId(surveyId);
        List<DefaultQuestionDto> infoList = questionList.stream().map(DefaultQuestionDto::fromEntity).toList();

        return new ParticipationQuestionResponse(infoList);
    }

    @Override
    public List<ChoiceOption> getOptionsByQuestionId(Long questionId) {
        return choiceOptionRepository.getOptionsByQuestionId(questionId);
    }
}
