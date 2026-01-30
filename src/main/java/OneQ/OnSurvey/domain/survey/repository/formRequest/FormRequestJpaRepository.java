package OneQ.OnSurvey.domain.survey.repository.formRequest;

import OneQ.OnSurvey.domain.survey.entity.FormRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormRequestJpaRepository extends JpaRepository<FormRequest, Long> {
    List<FormRequest> findByIsRegisteredFalse();
}
