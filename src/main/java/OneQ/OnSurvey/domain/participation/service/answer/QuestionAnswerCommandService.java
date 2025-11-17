package OneQ.OnSurvey.domain.participation.service.answer;

import OneQ.OnSurvey.domain.participation.entity.MemberSurveyStatus;
import OneQ.OnSurvey.domain.participation.entity.QuestionAnswer;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;
import OneQ.OnSurvey.domain.participation.repository.answer.AnswerRepository;
import OneQ.OnSurvey.domain.participation.repository.memberSurveyStatus.MemberSurveyStatusRepository;
import org.springframework.stereotype.Service;

@Service
public class QuestionAnswerCommandService extends AnswerCommandService<QuestionAnswer> {
    public QuestionAnswerCommandService(
        AnswerRepository<QuestionAnswer> answerRepository,
        MemberSurveyStatusRepository memberSurveyStatusRepository
    ) {
        super(answerRepository, memberSurveyStatusRepository);
    }

    @Override
    public QuestionAnswer createAnswerFromDto(AnswerInsertDto.AnswerInfo answerInfo) {
        return QuestionAnswer.from(answerInfo);
    }

    @Override
    public MemberSurveyStatus createMemberSurveyStatus(
        Long surveyId,
        AnswerInsertDto.AnswerInfo answerInfo
    ) {
        MemberSurveyStatus status = MemberSurveyStatus.of(
            surveyId,
            answerInfo.getMemberId(),
            true
        );
        return memberSurveyStatusRepository.save(status);
    }
}
