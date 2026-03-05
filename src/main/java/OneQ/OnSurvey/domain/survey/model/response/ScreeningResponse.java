package OneQ.OnSurvey.domain.survey.model.response;

import lombok.Builder;

@Builder
public record ScreeningResponse(
    Long screeningId,
    Long surveyId,
    String content,
    Boolean answer
) {
}
