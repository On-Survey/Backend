package OneQ.OnSurvey.domain.survey.service;

import OneQ.OnSurvey.domain.survey.entity.Screening;
import OneQ.OnSurvey.domain.survey.model.response.SurveyFormResponse;

public interface SurveyCommand {
    SurveyFormResponse upsertSurvey(Long surveyId, String title, String description, Long memberId);
    Boolean submitSurvey(Long surveyId);
    Boolean deleteById(Long surveyId);

    Screening upsertScreening(Long screeningId, Long surveyId, String content, Boolean answer);
}
