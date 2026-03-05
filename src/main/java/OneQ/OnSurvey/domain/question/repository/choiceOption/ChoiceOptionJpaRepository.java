package OneQ.OnSurvey.domain.question.repository.choiceOption;

import OneQ.OnSurvey.domain.question.entity.ChoiceOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ChoiceOptionJpaRepository extends JpaRepository<ChoiceOption, Long> {
    List<ChoiceOption> getChoiceOptionsByQuestionId(Long questionId);

    List<ChoiceOption> getChoiceOptionsByQuestionIdIsInOrderByChoiceOptionIdAsc(Collection<Long> questionIds);
}
