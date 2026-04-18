package OneQ.OnSurvey.domain.participation.model.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ParticipationCompletionDto(
    long surveyId,
    long memberId,
    long userKey,
    List<Integer> sectionList
) {
}
