package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.question.model.dto.QuestionUpsertDto;

import java.util.List;

public record UpdateQuestionResponse (
    Long surveyId,
    List<QuestionUpsertDto.UpsertInfo> info
) {
}
