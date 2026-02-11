package OneQ.OnSurvey.domain.admin.api.dto.response;

import OneQ.OnSurvey.domain.admin.domain.model.survey.AdminSurveyListView;

public record AdminSurveyIntroItem(
    Long surveyId,
    String title,
    String status,
    Long memberId,
    String createdAt
) {

    public static AdminSurveyIntroItem from(AdminSurveyListView surveyListView) {
        return new AdminSurveyIntroItem(
            surveyListView.surveyId(),
            surveyListView.title(),
            surveyListView.status().name(),
            surveyListView.memberId(),
            surveyListView.createdAt().toString()
        );
    }
}
