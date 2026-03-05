package OneQ.OnSurvey.domain.survey.model.dto;

import lombok.Builder;

@Builder
public record SurveyOwnerChangeDto (
    Long surveyId,
    Long newMemberId,
    Long newUserKey
) {
}
