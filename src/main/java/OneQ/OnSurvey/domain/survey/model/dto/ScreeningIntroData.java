package OneQ.OnSurvey.domain.survey.model.dto;

public record ScreeningIntroData (
    Long screeningId,
    Long surveyId,
    String content,
    Boolean answer,
    Long count
) {
}
