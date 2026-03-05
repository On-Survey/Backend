package OneQ.OnSurvey.domain.admin.api.dto.request;

public record ChangeSurveyOwnerRequest(
    Long newMemberId,
    Long newUserKey
) {
}
