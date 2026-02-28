package OneQ.OnSurvey.domain.survey.model.formRequest.event;

import java.util.List;

public record FormRequestConversionEvent (
    Long requestId,
    List<String> formUrls
) {
}
