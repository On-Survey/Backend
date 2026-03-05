package OneQ.OnSurvey.domain.survey.service.form;

import OneQ.OnSurvey.domain.survey.model.request.*;
import OneQ.OnSurvey.domain.survey.model.response.*;

public interface SurveyFormUseCase {
    SurveyFormResponse createSurvey(Long memberId, SurveyFormCreateRequest request);
    SurveyFormResponse updateSurveyDisplay(Long memberId, Long surveyId, SurveyFormCreateRequest request);
    CreateQuestionResponse createQuestion(Long surveyId, QuestionRequest request);
    UpdateQuestionResponse upsertQuestions(Long surveyId, QuestionRequest request);
    SurveyFormResponse completeSurvey(Long userKey, Long surveyId, SurveyFormRequest request);
    SurveyFormResponse completeFreeSurvey(Long userKey, Long surveyId, FreeSurveyFormRequest request);
    InterestResponse updateInterest(Long surveyId, SurveyInterestRequest request);
    ScreeningResponse createScreening(Long surveyId, ScreeningRequest request);
    SectionResponse upsertSection(Long surveyId, SectionRequest request);
}
