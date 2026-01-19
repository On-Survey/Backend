package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.survey.entity.Survey;

import java.time.LocalDateTime;
import java.util.Set;

public record ParticipationInfoResponse(
    Long surveyId,
    String title,
    String description,
    LocalDateTime deadline,
    Set<Interest> interests,
    Integer responseCount,
    Boolean isScreenRequired,
    Boolean isFree
) {
    public static ParticipationInfoResponse from(
        Survey survey, int responseCount, boolean isScreenRequired
    ) {
        return new ParticipationInfoResponse(
            survey.getId(),
            survey.getTitle(),
            survey.getDescription(),
            survey.getDeadline(),
            survey.getInterests(),
            responseCount,
            isScreenRequired,
            survey.getIsFree()
        );
    }
}
