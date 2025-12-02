package OneQ.OnSurvey.domain.question.repository.question;

import OneQ.OnSurvey.domain.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionJpaRepository extends JpaRepository<Question, Long> {
    List<Question> getQuestionsBySurveyIdOrderByOrder(Long surveyId);
}
