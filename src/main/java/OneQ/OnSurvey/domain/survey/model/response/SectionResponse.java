package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.question.model.dto.SectionDto;

import java.util.List;

public record SectionResponse (
    List<SectionDto> sections
) {

    public static SectionResponse from(List<SectionDto> sections) {
        return new SectionResponse(sections);
    }
}
