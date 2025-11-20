package OneQ.OnSurvey.domain.survey.repository;

import OneQ.OnSurvey.domain.survey.entity.SurveyInfo;

import java.util.List;
import java.util.Optional;

public interface SurveyInfoRepository {
    SurveyInfo save(SurveyInfo surveyInfo);
    Optional<SurveyInfo> findBySurveyId(Long surveyId);
    List<SurveyInfo> findBySurveyIdIn(List<Long> surveyIds);
}
