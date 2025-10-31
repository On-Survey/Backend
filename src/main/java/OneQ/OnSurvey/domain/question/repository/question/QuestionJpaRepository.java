package OneQ.OnSurvey.domain.question.repository.question;

import OneQ.OnSurvey.domain.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface QuestionJpaRepository extends JpaRepository<Question, Long> {
    List<Question> getQuestionsBySurveyId(Long surveyId);

    Question getQuestionByQuestionId(Long questionId);

    List<Question> getQuestionsByQuestionIdContains(Long questionId);

    List<Question> getQuestionsByQuestionIdIn(Collection<Long> questionIds);
}
