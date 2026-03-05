package OneQ.OnSurvey.domain.participation.service.response;

import OneQ.OnSurvey.domain.survey.model.SurveyResponseFilterCondition;

public interface ResponseQuery {
    Integer getResponseCountBySurveyId(Long surveyId);
    default Integer getResponseCountBySurveyId(
            Long surveyId,
            SurveyResponseFilterCondition filter
    ) {
        return getResponseCountBySurveyId(surveyId);
    }
}
