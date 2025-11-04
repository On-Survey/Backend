package OneQ.OnSurvey.domain.participation.service.answer;

import OneQ.OnSurvey.domain.participation.entity.AbstractAnswer;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;

import java.util.List;

public interface AnswerCommand<E extends AbstractAnswer> {
    List<E> insertAnswers(AnswerInsertDto insertDto);
    E insertAnswer(AnswerInsertDto.AnswerInfo answerInfo);
}
