package OneQ.OnSurvey.domain.survey.repository.formRequest;

import OneQ.OnSurvey.domain.survey.entity.FormRequest;

import java.util.List;
import java.util.Optional;

public interface FormRequestRepository {
    FormRequest save(FormRequest request);
    Optional<FormRequest> findById(Long id);
    List<FormRequest> findAllUnregistered();
}
