package OneQ.OnSurvey.domain.survey.model.formRequest;

import java.util.List;

public record FormValidationPayload(
    List<String> urls,
    String requesterEmail,
    Boolean isEmailRequired
) { }
