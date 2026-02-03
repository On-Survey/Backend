package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;

import java.util.List;

public record ParticipationQuestionResponse(
    String sectionTitle,
    String sectionDescription,
    Integer currSection,
    Integer nextSection,
    List<DefaultQuestionDto> info
) {
    public static ParticipationQuestionResponse of(
        List<DefaultQuestionDto> info
    ) {
        return new ParticipationQuestionResponse(
            null, null,
            1, 0,
            info
        );
    }

    public static ParticipationQuestionResponse of(
        String title,
        String description,
        Integer currSection,
        Integer nextSection,
        List<DefaultQuestionDto> info
    ) {
        return new ParticipationQuestionResponse(
            title, description,
            currSection != null ? currSection : 1,
            currSection != null ? nextSection : 0,
            info
        );
    }
}
