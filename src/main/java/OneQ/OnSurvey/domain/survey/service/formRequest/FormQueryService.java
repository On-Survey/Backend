package OneQ.OnSurvey.domain.survey.service.formRequest;

import OneQ.OnSurvey.domain.survey.entity.FormRequest;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormListResponse;
import OneQ.OnSurvey.domain.survey.repository.formRequest.FormRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FormQueryService implements FormFinder {

    private final FormRequestRepository googleFormRequestRepository;

    @Override
    public FormListResponse getAllUnregisteredRequests() {
        List<FormRequest> requests = googleFormRequestRepository.findAllUnregistered();
        return FormListResponse.of(requests);
    }
}
