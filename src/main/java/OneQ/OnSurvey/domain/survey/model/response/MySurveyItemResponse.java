package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.global.common.annotation.DateFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MySurveyItemResponse(
        Long surveyId,
        String title,
        SurveyStatus status,
        int totalCoin,
        @DateFormat LocalDate createdDate,
        @DateFormat LocalDateTime deadline
) { }
