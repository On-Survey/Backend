package OneQ.OnSurvey.domain.survey.model.response;

public record SurveyFormResponse (
    Long surveyId,
    String title,
    String description
) {
}
