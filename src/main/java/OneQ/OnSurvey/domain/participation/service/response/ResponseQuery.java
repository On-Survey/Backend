package OneQ.OnSurvey.domain.participation.service.response;

import OneQ.OnSurvey.domain.participation.entity.Response;

import java.util.List;

public interface ResponseQuery {
    List<Response> getResponsesByMemberId(Long memberId);
    List<Response> getResponsesBySurveyId(Long surveyId);

    Response getResponseBySurveyIdAndMemberId(Long surveyId, Long memberId);

    Integer getResponseCountBySurveyId(Long surveyId);
}
