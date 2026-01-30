package OneQ.OnSurvey.domain.survey.service.formRequest;

import OneQ.OnSurvey.domain.survey.entity.FormRequest;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormListResponse;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormRequestResponse;
import OneQ.OnSurvey.domain.survey.repository.formRequest.FormRequestRepository;
import OneQ.OnSurvey.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static OneQ.OnSurvey.domain.survey.SurveyErrorCode.FORM_REQUEST_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FormQueryService implements FormFinder {

    private final FormRequestRepository googleFormRequestRepository;

    @Override
    public FormRequestResponse getGoogleFormRequest(Long id) {
        FormRequest request = googleFormRequestRepository.findById(id)
                .orElseThrow(() -> new CustomException(FORM_REQUEST_NOT_FOUND));

        return FormRequestResponse.of(request);
    }

    @Override
    public FormListResponse getAllUnregisteredRequests() {
        List<FormRequest> requests = googleFormRequestRepository.findAllUnregistered();
        return FormListResponse.of(requests);
    }
}
