package OneQ.OnSurvey.domain.question.model.dto;

import OneQ.OnSurvey.domain.question.entity.Section;
import com.fasterxml.jackson.annotation.JsonIgnore;

public record SectionDto (
    Long sectionId,
    String title,
    String description,
    Integer order,
    Integer nextSection,
    String imageUrl
) {

    public static SectionDto fromEntity(Section section) {
        return new SectionDto(
            section.getSectionId(),
            section.getTitle(),
            section.getDescription(),
            section.getSectionOrder(),
            section.getNextSection(),
            section.getImageUrl()
        );
    }

    @JsonIgnore
    public boolean isValid() {
        return title != null && !title.strip().isBlank()
            && order != null && order >= 0;
    }

    @JsonIgnore
    public boolean isNewSection() {
        return sectionId == null;
    }
}
