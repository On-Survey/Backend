package OneQ.OnSurvey.domain.participation.service.response;

import OneQ.OnSurvey.domain.participation.entity.Response;
import OneQ.OnSurvey.domain.survey.model.SurveyResponseFilterCondition;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ResponseQuery {
    List<Response> getResponsesByMemberId(Long memberId);
    List<Response> getResponsesBySurveyId(Long surveyId);

    Response getResponseBySurveyIdAndMemberId(Long surveyId, Long memberId);

    Integer getResponseCountBySurveyId(Long surveyId);
    default Integer getResponseCountBySurveyId(
            Long surveyId,
            SurveyResponseFilterCondition filter
    ) {
        return getResponseCountBySurveyId(surveyId);
    }

    Map<Long, Long> getResponseCountsBySurveyIds(Collection<Long> surveyIds);
}
