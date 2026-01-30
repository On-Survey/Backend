package OneQ.OnSurvey.domain.survey.repository.formRequest;

import OneQ.OnSurvey.domain.survey.entity.FormRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FormRequestRepositoryImpl implements FormRequestRepository {

    private final FormRequestJpaRepository formRequestJpaRepository;

    @Override
    public FormRequest save(FormRequest request) {
        return formRequestJpaRepository.save(request);
    }

    @Override
    public Optional<FormRequest> findById(Long id) {
        return formRequestJpaRepository.findById(id);
    }

    @Override
    public List<FormRequest> findAllUnregistered() {
        return formRequestJpaRepository.findByIsRegisteredFalse();
    }
}
