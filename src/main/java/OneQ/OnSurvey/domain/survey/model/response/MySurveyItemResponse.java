package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.global.annotation.DateFormat;

import java.time.LocalDate;

public record MySurveyItemResponse(
        Long surveyId,
        String title,
        SurveyStatus status,
        int totalCoin,
        @DateFormat LocalDate createdDate
) { }
