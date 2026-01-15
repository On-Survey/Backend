package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;

import java.util.List;

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
