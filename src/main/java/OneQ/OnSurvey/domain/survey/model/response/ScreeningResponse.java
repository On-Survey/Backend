package OneQ.OnSurvey.domain.survey.model.response;

public record ScreeningResponse(
    Long screeningId,
    Long surveyId,
    String content,
    Boolean answer
) {
}
