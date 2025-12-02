package OneQ.OnSurvey.domain.question.repository.choiceOption;

import OneQ.OnSurvey.domain.question.entity.ChoiceOption;

import java.util.Collection;
import java.util.List;

public interface ChoiceOptionRepository {
    List<ChoiceOption> getOptionsByQuestionIds(Collection<Long> questionIdList);
    List<ChoiceOption> getOptionsByQuestionId(Long questionId);
    List<ChoiceOption> saveAll(Collection<ChoiceOption> choiceOptions);
    void deleteAll(Collection<Long> idList);
}
