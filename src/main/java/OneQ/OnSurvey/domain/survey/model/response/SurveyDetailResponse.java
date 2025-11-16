package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.entity.SurveyInfo;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.global.annotation.DateFormat;

import java.time.LocalDateTime;

public record SurveyDetailResponse(
        Long surveyId,
        String title,
        SurveyStatus status,
        int totalCoin,
        @DateFormat LocalDateTime createdAt,
        @DateFormat LocalDateTime deadline,
        SurveyInfoResponse surveyInfo
) {
    public static SurveyDetailResponse from(Survey survey, SurveyInfo info) {
        return new SurveyDetailResponse(
                survey.getId(),
                survey.getTitle(),
                survey.getStatus(),
                survey.getTotalCoin(),
                survey.getCreatedAt(),
                survey.getDeadline(),
                SurveyInfoResponse.from(info)
        );
    }
}
