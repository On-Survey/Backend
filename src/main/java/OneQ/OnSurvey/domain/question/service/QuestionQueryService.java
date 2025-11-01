package OneQ.OnSurvey.domain.question.service;

import OneQ.OnSurvey.domain.question.entity.ChoiceOption;
import OneQ.OnSurvey.domain.question.entity.Question;
import OneQ.OnSurvey.domain.question.repository.choiceOption.ChoiceOptionRepository;
import OneQ.OnSurvey.domain.question.repository.question.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionQueryService implements QuestionQuery {
    private final QuestionRepository questionRepository;
    private final ChoiceOptionRepository choiceOptionRepository;

    @Override
    public List<Question> getQuestionListBySurveyId(Long surveyId) {
        return questionRepository.getQuestionListBySurveyId(surveyId);
    }

    @Override
    public List<ChoiceOption> getOptionsByQuestionId(Long questionId) {
        return choiceOptionRepository.getOptionsByQuestionId(questionId);
    }
}
