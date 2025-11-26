package OneQ.OnSurvey.domain.participation.repository.response;

import OneQ.OnSurvey.domain.participation.entity.Response;
import OneQ.OnSurvey.domain.survey.model.SurveyResponseFilterCondition;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ResponseRepository {
    List<Response> getResponsesByMemberId(Long memberId);
    List<Response> getResponsesBySurveyId(Long surveyId);
    Response getResponseBySurveyIdAndMemberId(Long surveyId, Long memberId);
    Integer getResponseCountBySurveyId(Long surveyId);
    Integer getResponseCountBySurveyId(Long surveyId, SurveyResponseFilterCondition filter);
    Map<Long, Long> getResponseCountsBySurveyIds(Collection<Long> surveyIds);

    Response save(Response response);
    boolean existsBySurveyIdAndMemberId(Long surveyId, Long memberId);
}
