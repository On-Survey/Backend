package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;
import OneQ.OnSurvey.domain.survey.entity.Survey;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record ParticipationQuestionResponse(
    List<DefaultQuestionDto> info
) {

    public static ParticipationQuestionResponse of(
        List<DefaultQuestionDto> info
    ) {
        return new ParticipationQuestionResponse(
            info
        );
    }
}
