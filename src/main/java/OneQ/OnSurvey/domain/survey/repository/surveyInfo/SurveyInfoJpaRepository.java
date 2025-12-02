package OneQ.OnSurvey.domain.survey.repository.surveyInfo;

import OneQ.OnSurvey.domain.survey.entity.SurveyInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyInfoJpaRepository extends JpaRepository<SurveyInfo,Long> {
    List<SurveyInfo> findBySurveyIdIn(List<Long> surveyIds);
}
