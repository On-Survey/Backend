package OneQ.OnSurvey.domain.participation.service.answer;

import OneQ.OnSurvey.domain.participation.entity.MemberSurveyStatus;
import OneQ.OnSurvey.domain.participation.entity.QuestionAnswer;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;
import OneQ.OnSurvey.domain.participation.repository.answer.AnswerRepository;
import OneQ.OnSurvey.domain.participation.repository.memberSurveyStatus.MemberSurveyStatusRepository;
import OneQ.OnSurvey.domain.question.repository.question.QuestionRepository;
import org.springframework.stereotype.Service;

@Service
public class QuestionAnswerCommandService extends AnswerCommandService<QuestionAnswer> {
    private final QuestionRepository questionRepository;

    public QuestionAnswerCommandService(
        AnswerRepository<QuestionAnswer> answerRepository,
        MemberSurveyStatusRepository memberSurveyStatusRepository,
        QuestionRepository questionRepository
    ) {
        super(answerRepository, memberSurveyStatusRepository);
        this.questionRepository = questionRepository;
    }

    @Override
    public QuestionAnswer createAnswerFromDto(AnswerInsertDto.AnswerInfo answerInfo) {
        return QuestionAnswer.from(answerInfo);
    }

    @Override
    public Boolean insertAnswers(AnswerInsertDto insertDto) {
        Boolean result = super.insertAnswers(insertDto);
        Long surveyId = getSurveyIdFromQuestion(insertDto.getAnswerInfoList().getFirst().getId());
        createMemberSurveyStatus(surveyId, insertDto.getAnswerInfoList().getFirst());

        return true;
    }

    private Long getSurveyIdFromQuestion(Long questionId) {
        return questionRepository.getSurveyId(questionId);
    }

    @Override
    public MemberSurveyStatus createMemberSurveyStatus(
        Long surveyId,
        AnswerInsertDto.AnswerInfo answerInfo
    ) {
        MemberSurveyStatus status = memberSurveyStatusRepository.getMemberSurveyStatus(surveyId, answerInfo.getMemberId());

        if (status != null) {
            status.updateResponseStatus(true);
        } else {
            status = MemberSurveyStatus.of(
                surveyId,
                answerInfo.getMemberId(),
                true
            );
        }
        return memberSurveyStatusRepository.save(status);
    }
}
