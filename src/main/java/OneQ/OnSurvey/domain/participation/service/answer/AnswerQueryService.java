package OneQ.OnSurvey.domain.participation.service.answer;

import OneQ.OnSurvey.domain.participation.entity.AbstractAnswer;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;
import OneQ.OnSurvey.domain.participation.repository.answer.AnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
public abstract class AnswerQueryService<E extends AbstractAnswer> implements AnswerQuery<E> {
    protected final AnswerRepository<E> answerRepository;

    public List<E> getAnswersByIdListAndMemberId(List<Long> idList, Long memberId) {
        return answerRepository.getAnswersByQuestionIdListAndMemberId(idList, memberId);
    }

    protected abstract E createAnswerFromDto(AnswerInsertDto.AnswerInfo answerInfo);
}
