package OneQ.OnSurvey.domain.survey.service.formRequest;

import OneQ.OnSurvey.domain.survey.model.formRequest.FormRequestDto;

public interface FormCreator {
    Long createFormRequest(FormRequestDto dto);
}
