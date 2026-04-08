package OneQ.OnSurvey.domain.admin.api.dto.response;

import OneQ.OnSurvey.domain.admin.domain.model.survey.OngoingSurveyView;

public record OngoingSurveyResponse(
    Long surveyId,
    String title,
    int completedCount,
    int dueCount
) {
    public static OngoingSurveyResponse from(OngoingSurveyView view) {
        return new OngoingSurveyResponse(
            view.surveyId(),
            view.title(),
            view.completedCount(),
            view.dueCount()
        );
    }
}
