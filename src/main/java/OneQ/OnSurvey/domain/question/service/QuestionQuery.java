package OneQ.OnSurvey.domain.question.service;

import OneQ.OnSurvey.domain.question.entity.ChoiceOption;
import OneQ.OnSurvey.domain.survey.model.response.ParticipationQuestionResponse;

import java.util.List;

public interface QuestionQuery {
    // DefaultQuestionDto 사용하도록 수정
    ParticipationQuestionResponse getQuestionListBySurveyId(Long surveyId);
    List<ChoiceOption> getOptionsByQuestionId(Long questionId);
}
