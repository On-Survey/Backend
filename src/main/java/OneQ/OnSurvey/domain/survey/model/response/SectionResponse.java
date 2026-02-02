package OneQ.OnSurvey.domain.survey.model.response;

public record SectionResponse (
    Long sectionId,
    String title,
    String description,
    Integer order,
    Integer nextSection
) {
}
