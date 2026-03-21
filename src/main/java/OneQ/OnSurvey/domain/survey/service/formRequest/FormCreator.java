package OneQ.OnSurvey.domain.survey.service.formRequest;

import OneQ.OnSurvey.domain.survey.model.formRequest.FormValidationRequestDto;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormValidationResponse;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormRequestDto;

public interface FormCreator {
    Long createFormRequest(FormRequestDto dto);
    FormValidationResponse validationFormRequestLink(FormValidationRequestDto dto);
}
