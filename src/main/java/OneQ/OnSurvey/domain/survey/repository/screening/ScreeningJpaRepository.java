package OneQ.OnSurvey.domain.survey.repository.screening;

import OneQ.OnSurvey.domain.survey.entity.Screening;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScreeningJpaRepository extends JpaRepository<Screening, Long> {
    Screening getScreeningBySurveyId(Long surveyId);
}
