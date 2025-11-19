package OneQ.OnSurvey.domain.participation.repository.response;

import OneQ.OnSurvey.domain.participation.entity.Response;

import java.util.List;

public interface ResponseRepository {
    List<Response> getResponsesByMemberId(Long memberId);
    List<Response> getResponsesBySurveyId(Long surveyId);
    Response getResponseBySurveyIdAndMemberId(Long surveyId, Long memberId);
    Integer getResponseCountBySurveyId(Long surveyId);

    Response save(Response response);
    boolean existsBySurveyIdAndMemberId(Long surveyId, Long memberId);
}
