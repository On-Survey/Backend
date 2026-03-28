package OneQ.OnSurvey.domain.survey.service.formRequest;

import OneQ.OnSurvey.domain.survey.model.formRequest.FormValidationRequestDto;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormValidationResponse;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormRequestDto;

public interface FormCreator {
    Long createFormRequest(Long userKey, Long memberId, FormRequestDto dto);
    FormValidationResponse validationFormRequestLink(Long userKey, FormValidationRequestDto dto);
}
