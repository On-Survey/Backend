package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.survey.model.dto.ParticipationInfoVO;

import java.time.LocalDateTime;
import java.util.Set;

public record ParticipationInfoResponse(
    Long surveyId,
    String title,
    String description,
    Integer totalSections,
    LocalDateTime deadline,
    Set<Interest> interests,
    Integer responseCount,
    boolean isScreenRequired,
    boolean isScreened,
    boolean isSurveyResponded,
    Boolean isFree
) {
    public static ParticipationInfoResponse from(
        ParticipationInfoVO vo, int responseCount
    ) {
        return new ParticipationInfoResponse(
            vo.surveyId(), vo.title(), vo.description(), vo.totalSections(), vo.deadline(), vo.interests(), responseCount,
            vo.participationStatus().isScreenRequired(), vo.participationStatus().isScreened(), vo.participationStatus().isSurveyResponded(),
            vo.isFree()
        );
    }
}
