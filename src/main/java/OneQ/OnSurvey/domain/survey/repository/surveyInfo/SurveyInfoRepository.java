package OneQ.OnSurvey.domain.survey.repository.surveyInfo;

import OneQ.OnSurvey.domain.survey.entity.SurveyInfo;
import OneQ.OnSurvey.domain.survey.model.dto.SurveySegmentation;

import java.util.List;
import java.util.Optional;

public interface SurveyInfoRepository {
    SurveyInfo save(SurveyInfo surveyInfo);
    Optional<SurveyInfo> findBySurveyId(Long surveyId);
    List<SurveyInfo> findBySurveyIdIn(List<Long> surveyIds);
    void increaseCompletedCount(Long surveyId);

    SurveySegmentation findSegmentationBySurveyId(Long surveyId);
}
