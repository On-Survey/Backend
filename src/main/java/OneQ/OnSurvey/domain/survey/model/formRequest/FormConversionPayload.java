package OneQ.OnSurvey.domain.survey.model.formRequest;

import java.util.List;

public record FormConversionPayload (
    Long requestId,
    List<String> formUrls
) {
}
