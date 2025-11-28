package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;
import OneQ.OnSurvey.domain.survey.entity.Survey;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record ParticipationQuestionResponse(
        Long surveyId,
        Long memberId,
        String title,
        String description,
        Set<Interest> interests,
        LocalDateTime deadline,
        List<DefaultQuestionDto> info
) {

    public static ParticipationQuestionResponse of(
            Survey survey,
            List<DefaultQuestionDto> info
    ) {
        Set<Interest> interestsSet = survey.getInterests() != null
                        ? new java.util.HashSet<>(survey.getInterests())
                        : java.util.Collections.emptySet();

        return new ParticipationQuestionResponse(
                survey.getId(),
                survey.getMemberId(),
                survey.getTitle(),
                survey.getDescription(),
                interestsSet,
                survey.getDeadline(),
                info
        );
    }
}
