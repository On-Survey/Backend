package OneQ.OnSurvey.domain.admin.domain.model.survey;

public record OngoingSurveyView(
    Long surveyId,
    String title,
    int completedCount,
    int dueCount
) { }
