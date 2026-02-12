package OneQ.OnSurvey.domain.admin.api.dto.request;

import OneQ.OnSurvey.domain.admin.domain.model.survey.AdminSurveyStatus;

public record AdminSurveySearchQuery(
    AdminSurveyStatus status,
    String keyword,
    Long creator
) {
}
