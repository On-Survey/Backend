package OneQ.OnSurvey.domain.participation.repository.response;

import OneQ.OnSurvey.domain.participation.entity.Response;
import OneQ.OnSurvey.domain.survey.model.SurveyResponseFilterCondition;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ResponseRepository {
    Integer getResponseCountBySurveyId(Long surveyId);
    Integer getResponseCountBySurveyId(Long surveyId, SurveyResponseFilterCondition filter);
    Map<Long, Long> getResponseCountsBySurveyIds(Collection<Long> surveyIds);

    Response save(Response response);
    List<Long> getExcludedSurveyIdList(Long memberId, boolean checkScreened);
    Optional<Response> findBySurveyIdAndMemberId(Long surveyId, Long memberId);
}

