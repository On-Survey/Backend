package OneQ.OnSurvey.domain.participation.service.answer;

import OneQ.OnSurvey.domain.participation.entity.MemberSurveyStatus;
import OneQ.OnSurvey.domain.participation.entity.ScreeningAnswer;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;
import OneQ.OnSurvey.domain.participation.repository.answer.AnswerRepository;
import OneQ.OnSurvey.domain.participation.repository.memberSurveyStatus.MemberSurveyStatusRepository;
import OneQ.OnSurvey.domain.survey.repository.screening.ScreeningRepository;
import org.springframework.stereotype.Service;

@Service
public class ScreeningAnswerCommandService extends AnswerCommandService<ScreeningAnswer> {
    private final ScreeningRepository screeningRepository;

    public ScreeningAnswerCommandService(
        AnswerRepository<ScreeningAnswer> answerRepository,
        MemberSurveyStatusRepository memberSurveyStatusRepository,
        ScreeningRepository screeningRepository
    ) {
        super(answerRepository, memberSurveyStatusRepository);
        this.screeningRepository = screeningRepository;
    }

    @Override
    public ScreeningAnswer createAnswerFromDto(AnswerInsertDto.AnswerInfo answerInfo) {
        return ScreeningAnswer.from(answerInfo);
    }

    @Override
    public Boolean insertAnswer(AnswerInsertDto.AnswerInfo answerInfo) {
        super.insertAnswer(answerInfo);
        Long surveyId = getSurveyIdFromScreening(answerInfo.getId());
        createMemberSurveyStatus(surveyId, answerInfo);

        return true;
    }

    private Long getSurveyIdFromScreening(Long screeningId) {
        return screeningRepository.getSurveyId(screeningId);
    }

    @Override
    public MemberSurveyStatus createMemberSurveyStatus(
        Long surveyId,
        AnswerInsertDto.AnswerInfo answerInfo
    ) {
        boolean answer = screeningRepository.getScreeningAnswer(answerInfo.getId());

        if (answer != answerInfo.getBooleanContent()) {
            MemberSurveyStatus status = MemberSurveyStatus.of(
                surveyId,
                answerInfo.getMemberId(),
                false,
                true
            );
            return memberSurveyStatusRepository.save(status);
        }

        return null;
    }
}
