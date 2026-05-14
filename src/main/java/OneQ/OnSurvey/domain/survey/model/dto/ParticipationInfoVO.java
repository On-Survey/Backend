package OneQ.OnSurvey.domain.survey.model.dto;

import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.participation.model.dto.ParticipationStatus;

import java.time.LocalDateTime;
import java.util.Set;

public record ParticipationInfoVO(
    Long surveyId,
    String title,
    String description,
    Integer totalSections,
    LocalDateTime deadline,
    Set<Interest> interests,
    ParticipationStatus participationStatus,
    Boolean isFree
) {
    public ParticipationInfoVO(
        Long surveyId,
        String title,
        String description,
        Integer totalSections,
        LocalDateTime deadline,
        Set<Interest> interests,
        Long screeningId,
        Boolean eIsScreened,
        Boolean eIsResponded,
        Boolean isFree
    ) {
        this(
            surveyId, title, description, totalSections, deadline, interests,
            ParticipationStatus.generateStatus(screeningId, eIsScreened, eIsResponded),
            isFree
        );
    }
}
