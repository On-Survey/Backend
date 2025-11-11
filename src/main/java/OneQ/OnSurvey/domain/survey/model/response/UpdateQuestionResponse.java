package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.question.model.dto.type.QuestionTypeAndInfoDto;

import java.util.List;

public record UpdateQuestionResponse (
    Long surveyId,
    List<QuestionTypeAndInfoDto> info
) {
}
