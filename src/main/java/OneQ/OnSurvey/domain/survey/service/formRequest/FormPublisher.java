package OneQ.OnSurvey.domain.survey.service.formRequest;

import OneQ.OnSurvey.domain.survey.model.formRequest.FormPublishRequest;
import OneQ.OnSurvey.domain.survey.model.response.SurveyFormResponse;

public interface FormPublisher {
    SurveyFormResponse publishFormRequest(Long requestId, FormPublishRequest request);
}
