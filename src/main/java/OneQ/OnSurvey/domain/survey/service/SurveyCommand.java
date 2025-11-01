package OneQ.OnSurvey.domain.survey.service;

import OneQ.OnSurvey.domain.survey.entity.Screening;
import OneQ.OnSurvey.domain.survey.entity.Survey;

public interface SurveyCommand {
    Survey upsertSurvey(Long surveyId, String title, String description);
    Survey submitSurvey(Long surveyId);
    Boolean deleteById(Long surveyId);

    Screening upsertScreening(Long screeningId, Long surveyId, String content, Boolean answer);
}
