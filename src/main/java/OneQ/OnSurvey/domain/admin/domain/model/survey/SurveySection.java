package OneQ.OnSurvey.domain.admin.domain.model.survey;

public record SurveySection (
    Long sectionId,
    String title,
    String description,
    Integer order,
    Integer nextSection
) {
}
