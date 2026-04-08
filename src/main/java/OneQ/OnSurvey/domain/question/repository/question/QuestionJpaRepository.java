package OneQ.OnSurvey.domain.question.repository.question;

import OneQ.OnSurvey.domain.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface QuestionJpaRepository extends JpaRepository<Question, Long> {
    List<Question> getQuestionsBySurveyIdOrderByOrder(Long surveyId);

    List<Question> getQuestionsBySurveyIdAndSectionOrderByOrder(Long surveyId, Integer section);

    void deleteAllBySurveyIdEqualsAndSectionNotIn(Long surveyId, Collection<Integer> sections);

    int countBySurveyId(Long surveyId);
}
