package OneQ.OnSurvey.domain.question.repository.gridOption;

import OneQ.OnSurvey.domain.question.entity.GridOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface GridOptionJpaRepository extends JpaRepository<GridOption, Long> {
    List<GridOption> getGridOptionsByQuestionIdIsInOrderByGridOptionIdAsc(Collection<Long> questionIds);

    List<GridOption> getGridOptionsByQuestionId(Long questionId);
}
