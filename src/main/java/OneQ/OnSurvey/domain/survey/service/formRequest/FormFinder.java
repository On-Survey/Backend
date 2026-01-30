package OneQ.OnSurvey.domain.survey.service.formRequest;

import OneQ.OnSurvey.domain.survey.model.formRequest.FormListResponse;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormRequestResponse;

public interface FormFinder {
    FormRequestResponse getGoogleFormRequest(Long id);
    FormListResponse getAllUnregisteredRequests();
}
