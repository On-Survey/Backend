package OneQ.OnSurvey.domain.participation.repository.answer;

import OneQ.OnSurvey.domain.participation.entity.AbstractAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerJpaRepository<E extends AbstractAnswer> extends JpaRepository<E, Long> {

}
