package OneQ.OnSurvey.domain.question.repository.choiceOption;

import OneQ.OnSurvey.domain.question.entity.ChoiceOption;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChoiceOptionRepositoryImpl implements ChoiceOptionRepository {
    private final ChoiceOptionJpaRepository choiceOptionJpaRepository;

    @Override
    public List<ChoiceOption> getOptionsByIds(Collection<Long> idList) {
        return choiceOptionJpaRepository.getChoiceOptionsByChoiceOptionIdIn(idList);
    }

    @Override
    public List<ChoiceOption> getOptionsByQuestionIds(Collection<Long> questionIdList) {
        return choiceOptionJpaRepository.getChoiceOptionsByQuestionIdIsIn(questionIdList);
    }

    @Override
    public List<ChoiceOption> getOptionsByQuestionId(Long questionId) {
        return choiceOptionJpaRepository.getChoiceOptionsByQuestionId(questionId);
    }

    @Override
    public List<ChoiceOption> saveAll(Collection<ChoiceOption> choiceOptions) {
        return choiceOptionJpaRepository.saveAllAndFlush(choiceOptions);
    }

    @Override
    public Boolean deleteAll(Collection<Long> optionIdList) {
        return choiceOptionJpaRepository.deleteAllByChoiceOptionIdIsIn(optionIdList);
    }
}
