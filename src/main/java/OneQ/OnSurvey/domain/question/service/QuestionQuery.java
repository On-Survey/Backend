package OneQ.OnSurvey.domain.question.service;

import OneQ.OnSurvey.domain.question.entity.ChoiceOption;
import OneQ.OnSurvey.domain.question.entity.Question;

import java.util.List;

public interface QuestionQuery {
    List<Question> getQuestionListBySurveyId(Long surveyId);

    List<ChoiceOption> getOptionsByQuestionId(Long questionId);
}
