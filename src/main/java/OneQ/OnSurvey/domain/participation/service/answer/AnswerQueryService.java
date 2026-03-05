package OneQ.OnSurvey.domain.participation.service.answer;

import OneQ.OnSurvey.domain.participation.entity.AbstractAnswer;
import OneQ.OnSurvey.domain.participation.repository.answer.AnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
public abstract class AnswerQueryService<E extends AbstractAnswer> implements AnswerQuery<E> {

    protected final AnswerRepository<E> answerRepository;

}
