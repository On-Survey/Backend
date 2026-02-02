package OneQ.OnSurvey.domain.survey.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record SectionRequest (
    Long sectionId,
    String title,
    String description,
    Integer order,
    Integer nextSection,
) {

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
