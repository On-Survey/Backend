package OneQ.OnSurvey.domain.survey.service.formRequest;

import OneQ.OnSurvey.domain.survey.entity.FormRequest;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormListResponse;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormRequestResponse;
import OneQ.OnSurvey.domain.survey.repository.formRequest.FormRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FormQueryService implements FormFinder {

    private final FormRequestRepository formRequestRepository;

    @Override
    public FormListResponse getAllUnregisteredRequests() {
        List<FormRequest> requests = formRequestRepository.findAllUnregistered();
        return FormListResponse.of(requests);
    }

    @Override
    public Page<FormRequestResponse> getFormRequests(String email, Boolean isRegistered, Pageable pageable) {
        Page<FormRequest> formRequestPage = formRequestRepository.findAllWithFilters(email, isRegistered, pageable);

        return formRequestPage.map(FormRequestResponse::of);
    }
}
