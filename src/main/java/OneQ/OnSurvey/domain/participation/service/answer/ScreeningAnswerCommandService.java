package OneQ.OnSurvey.domain.participation.service.answer;

import OneQ.OnSurvey.domain.member.service.MemberFinder;
import OneQ.OnSurvey.domain.participation.entity.ScreeningAnswer;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;
import OneQ.OnSurvey.domain.participation.repository.answer.AnswerRepository;
import org.springframework.stereotype.Service;

@Service
public class ScreeningAnswerCommandService extends AnswerCommandService<ScreeningAnswer> {
    public ScreeningAnswerCommandService(
        AnswerRepository<ScreeningAnswer> answerRepository,
        MemberFinder memberFinder
    ) {
        super(answerRepository, memberFinder);
    }

    @Override
    public ScreeningAnswer createAnswerFromDto(AnswerInsertDto.AnswerInfo answerInfo, Long memberId) {
        return ScreeningAnswer.from(answerInfo, memberId);
    }
}
