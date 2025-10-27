package OneQ.OnSurvey.domain.survey.repository;

import OneQ.OnSurvey.domain.survey.entity.SurveyInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyInfoJpaRepository extends JpaRepository<SurveyInfo,Long> {
}
