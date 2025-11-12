package OneQ.OnSurvey.domain.participation.service.answer;

import OneQ.OnSurvey.domain.member.service.MemberFinder;
import OneQ.OnSurvey.domain.participation.entity.ScreeningAnswer;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;
import OneQ.OnSurvey.domain.participation.repository.answer.AnswerRepository;
import org.springframework.stereotype.Service;

@Service
public class ScreeningAnswerQueryService extends AnswerQueryService<ScreeningAnswer> {
    public ScreeningAnswerQueryService(
        AnswerRepository<ScreeningAnswer> answerRepository
    ) {
        super(answerRepository);
    }

    @Override
    public ScreeningAnswer getAnswerById(Long id, Long memberId) {
        return answerRepository.getAnswerByQuestionIdAndMemberId(id, memberId);
    }

    @Override
    public ScreeningAnswer createAnswerFromDto(AnswerInsertDto.AnswerInfo answerInfo) {
        return ScreeningAnswer.from(answerInfo);
    }
}
