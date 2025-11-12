package OneQ.OnSurvey.domain.participation.service.answer;

import OneQ.OnSurvey.domain.participation.entity.QuestionAnswer;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;
import OneQ.OnSurvey.domain.participation.repository.answer.AnswerRepository;
import org.springframework.stereotype.Service;

@Service
public class QuestionAnswerCommandServiceImpl extends AnswerCommandService<QuestionAnswer> {
    public QuestionAnswerCommandServiceImpl(
        AnswerRepository<QuestionAnswer> answerRepository
    ) {
        super(answerRepository);
    }

    @Override
    public QuestionAnswer createAnswerFromDto(AnswerInsertDto.AnswerInfo answerInfo) {
        return QuestionAnswer.from(answerInfo);
    }
}
