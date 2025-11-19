package OneQ.OnSurvey.domain.participation.repository.response;

import OneQ.OnSurvey.domain.participation.entity.Response;
import OneQ.OnSurvey.domain.participation.entity.id.ResponseId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResponseJpaRepository extends JpaRepository<Response, ResponseId> {
    Integer countResponsesBySurveyId(Long surveyId);

    List<Response> getResponsesByMemberId(Long memberId);

    List<Response> getResponsesBySurveyId(Long surveyId);

    boolean existsBySurveyIdAndMemberId(Long surveyId, Long memberId);
}
