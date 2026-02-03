package OneQ.OnSurvey.domain.question.repository.section;

import OneQ.OnSurvey.domain.question.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionJpaRepository extends JpaRepository<Section, Long> {
    Section findBySurveyIdAndSectionOrder(Long surveyId, Integer sectionOrder);

    List<Section> findAllBySurveyId(Long surveyId);
}
