package OneQ.OnSurvey.domain.survey.service.refund;

import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.entity.SurveyInfo;

public interface SurveyRefundPolicy {
    int calculateRefundAmount(Survey survey, SurveyInfo surveyInfo);
}
