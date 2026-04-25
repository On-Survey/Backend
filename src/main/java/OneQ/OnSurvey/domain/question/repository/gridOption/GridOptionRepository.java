package OneQ.OnSurvey.domain.question.repository.gridOption;

import OneQ.OnSurvey.domain.question.entity.GridOption;

import java.util.Collection;
import java.util.List;

public interface GridOptionRepository {
    List<GridOption> getGridOptionsByQuestionIds(Collection<Long> questionIds);
    List<GridOption> getGridOptionsByQuestionId(Long questionId);
    List<GridOption> saveAll(Collection<GridOption> gridOptions);
    void deleteAll(Collection<Long> ids);
    void deleteBySections(Long surveyId, Collection<Integer> sections);
}
