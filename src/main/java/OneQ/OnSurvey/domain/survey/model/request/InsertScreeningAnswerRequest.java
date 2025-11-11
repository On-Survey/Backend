package OneQ.OnSurvey.domain.survey.model.request;

public record InsertScreeningAnswerRequest(
    Long screeningId,
    String content
) {

}
