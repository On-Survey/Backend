package OneQ.OnSurvey.domain.survey.repository.screening;

import OneQ.OnSurvey.domain.survey.entity.Screening;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ScreeningJpaRepository extends JpaRepository<Screening, Long> {
    Screening getScreeningBySurveyId(Long surveyId);

    List<Screening> getScreeningBySurveyIdGreaterThanEqualAndSurveyIdIsIn(Long surveyIdIsGreaterThan, Collection<Long> surveyIds);
}
