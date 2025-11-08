package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.question.model.QuestionType;
import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;

import java.util.List;
import java.util.Map;

public record UpdateQuestionResponse (
    Long surveyId,
    Map<QuestionType, List<DefaultQuestionDto>> info
) {
}
