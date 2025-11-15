package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;

import java.util.List;

public record FormQuestionResponse(
    List<DefaultQuestionDto> questions
) {
}
