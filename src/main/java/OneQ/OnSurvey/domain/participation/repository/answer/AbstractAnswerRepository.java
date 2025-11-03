package OneQ.OnSurvey.domain.participation.repository.answer;

import OneQ.OnSurvey.domain.participation.entity.AbstractAnswer;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public abstract class AbstractAnswerRepository<E extends AbstractAnswer> implements AnswerRepository<E> {
    protected final AnswerJpaRepository<E> answerJpaRepository;

    public E save(E answer) {
        return answerJpaRepository.save(answer);
    }

    public List<E> saveAll(Collection<E> answerList) {
        return answerJpaRepository.saveAll(answerList);
    }
}
