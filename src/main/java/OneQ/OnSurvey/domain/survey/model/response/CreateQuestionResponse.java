package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.question.model.QuestionType;

public record CreateQuestionResponse (
    Long surveyId,
    Long questionId,
    Integer order,
    String title,
    QuestionType type
) {
}
