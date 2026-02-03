package OneQ.OnSurvey.domain.survey.model.request;

import OneQ.OnSurvey.domain.question.model.dto.SectionDto;

import java.util.List;

public record SectionRequest (
    List<SectionInfo> sectionInfoList
) {
    public record SectionInfo (
        Long sectionId,
        String title,
        String description,
        Integer order,
        Integer nextSection
    ) { }

    public List<SectionDto> toDto() {
        return sectionInfoList.stream().map(sectionInfo -> new SectionDto(
            sectionInfo.sectionId(),
            sectionInfo.title(),
            sectionInfo.description(),
            sectionInfo.order(),
            sectionInfo.nextSection()
        )).toList();
    }
}
