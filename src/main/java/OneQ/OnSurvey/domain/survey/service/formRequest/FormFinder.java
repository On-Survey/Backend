package OneQ.OnSurvey.domain.survey.service.formRequest;

import OneQ.OnSurvey.domain.survey.model.formRequest.FormListResponse;

public interface FormFinder {
    FormListResponse getAllUnregisteredRequests();
}
