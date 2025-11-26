package OneQ.OnSurvey.domain.participation.service.response;

import OneQ.OnSurvey.domain.survey.model.SurveyResponseFilterCondition;

import java.util.Collection;
import java.util.Map;

public interface ResponseQuery {
    Integer getResponseCountBySurveyId(Long surveyId);
    default Integer getResponseCountBySurveyId(
            Long surveyId,
            SurveyResponseFilterCondition filter
    ) {
        return getResponseCountBySurveyId(surveyId);
    }

    Map<Long, Long> getResponseCountsBySurveyIds(Collection<Long> surveyIds);
}
