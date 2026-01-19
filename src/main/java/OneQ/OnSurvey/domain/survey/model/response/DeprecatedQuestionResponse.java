package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;
import OneQ.OnSurvey.domain.survey.entity.Survey;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record DeprecatedQuestionResponse(
    Long surveyId,
    Long memberId,
    String title,
    String description,
    Boolean isFree,
    Set<Interest> interests,
    LocalDateTime deadline,
    List<DefaultQuestionDto> info,
    boolean isScreenRequired
) {

    public static DeprecatedQuestionResponse of(
        Survey survey,
        List<DefaultQuestionDto> info,
        boolean isScreenRequired
    ) {
        Set<Interest> interestsSet = survey.getInterests() != null
            ? new java.util.HashSet<>(survey.getInterests())
            : java.util.Collections.emptySet();

        return new DeprecatedQuestionResponse(
            survey.getId(),
            survey.getMemberId(),
            survey.getTitle(),
            survey.getDescription(),
            survey.getIsFree(),
            interestsSet,
            survey.getDeadline(),
            info,
            isScreenRequired
        );
    }
}
