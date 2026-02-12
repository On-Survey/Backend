package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.question.model.dto.SectionDto;
import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;
import OneQ.OnSurvey.domain.survey.model.dto.ScreeningFormData;

import java.util.List;

public record FormQuestionResponse(
    Long surveyId,
    List<DefaultQuestionDto> questions,
    ScreeningFormData screening,
    List<SectionDto> sections
) {
}
