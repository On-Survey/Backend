package OneQ.OnSurvey.domain.survey.repository;

import OneQ.OnSurvey.domain.survey.entity.Survey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyJpaRepository extends JpaRepository<Survey, Long> {
    Survey getSurveyById(Long id);
    List<Survey> getSurveysByMemberId(Long memberId);

    Page<Survey> getSurveysByIdGreaterThan(Long idIsGreaterThan, Pageable pageable);
}
