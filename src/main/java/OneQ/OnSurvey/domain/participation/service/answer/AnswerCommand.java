package OneQ.OnSurvey.domain.participation.service.answer;

import OneQ.OnSurvey.domain.participation.entity.AbstractAnswer;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;

public interface AnswerCommand<E extends AbstractAnswer> {
    Boolean insertAnswers(AnswerInsertDto insertDto);
    Boolean insertAnswer(AnswerInsertDto.AnswerInfo answerInfo);
}
