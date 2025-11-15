package OneQ.OnSurvey.domain.survey.service;

import OneQ.OnSurvey.domain.survey.model.request.SurveyFormRequest;
import OneQ.OnSurvey.domain.survey.model.response.ScreeningResponse;
import OneQ.OnSurvey.domain.survey.model.response.SurveyFormResponse;

public interface SurveyCommand {
    SurveyFormResponse upsertSurvey(Long memberId, Long surveyId, SurveyFormRequest request);
    Boolean submitSurvey(Long surveyId);
    Boolean deleteById(Long surveyId);

    ScreeningResponse upsertScreening(Long screeningId, Long surveyId, String content, Boolean answer);
    Boolean refundSurvey(Long memberId, Long surveyId);
}