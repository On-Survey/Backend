package OneQ.OnSurvey.domain.participation.service.answer;

import OneQ.OnSurvey.domain.participation.entity.AbstractAnswer;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;
import OneQ.OnSurvey.domain.participation.repository.answer.AnswerRepository;
import OneQ.OnSurvey.domain.participation.repository.response.ResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@RequiredArgsConstructor
public abstract class AnswerCommandService<E extends AbstractAnswer> implements AnswerCommand<E> {

    private final AnswerRepository<E> answerRepository;
    protected final ResponseRepository responseRepository;

    public Boolean insertAnswers(AnswerInsertDto insertDto) {
        List<E> answerList = insertDto.getAnswerInfoList().stream()
            .map(this::createAnswerFromDto)
            .toList();
        answerRepository.saveAll(answerList);

        return true;
    }

    public Boolean insertAnswer(AnswerInsertDto.AnswerInfo answerInfo) {
        E answer = createAnswerFromDto(answerInfo);
        answerRepository.save(answer);

        return true;
    }

    protected abstract E createAnswerFromDto(AnswerInsertDto.AnswerInfo answerInfo);
}
