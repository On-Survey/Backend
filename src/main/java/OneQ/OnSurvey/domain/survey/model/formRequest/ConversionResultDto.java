package OneQ.OnSurvey.domain.survey.model.formRequest;

import OneQ.OnSurvey.domain.question.model.dto.SectionDto;
import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;

import java.util.List;

public record ConversionResultDto (
    String title,
    String description,
    List<SectionDto> sections,
    List<DefaultQuestionDto> questions
) { }
