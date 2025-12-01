package OneQ.OnSurvey.domain.participation.repository.response;

import OneQ.OnSurvey.domain.participation.entity.Response;
import OneQ.OnSurvey.domain.participation.entity.id.ResponseId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResponseJpaRepository extends JpaRepository<Response, ResponseId> {
    Optional<Response> findBySurveyIdAndMemberId(Long surveyId, Long memberId);
}
