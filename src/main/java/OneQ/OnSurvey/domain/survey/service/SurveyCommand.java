package OneQ.OnSurvey.domain.survey.service;

import OneQ.OnSurvey.domain.survey.entity.Survey;

public interface SurveyCommand {
    Survey upsertSurvey(Long surveyId, String title, String description);
    Boolean deleteById(Long surveyId);
}
