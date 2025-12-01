package OneQ.OnSurvey.domain.participation.service.answer;

import OneQ.OnSurvey.domain.participation.entity.Response;
import OneQ.OnSurvey.domain.participation.entity.ScreeningAnswer;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;
import OneQ.OnSurvey.domain.participation.repository.answer.AnswerRepository;
import OneQ.OnSurvey.domain.participation.repository.response.ResponseRepository;
import OneQ.OnSurvey.domain.survey.repository.screening.ScreeningRepository;
import org.springframework.stereotype.Service;

@Service
public class ScreeningAnswerCommandService extends AnswerCommandService<ScreeningAnswer> {

    private final ScreeningRepository screeningRepository;

    public ScreeningAnswerCommandService(
        AnswerRepository<ScreeningAnswer> answerRepository,
        ResponseRepository responseRepository,
        ScreeningRepository screeningRepository
    ) {
        super(answerRepository, responseRepository);
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
        updateResponseAfterScreening(surveyId, answerInfo);

        return true;
    }

    private Long getSurveyIdFromScreening(Long screeningId) {
        return screeningRepository.getSurveyId(screeningId);
    }

    public void updateResponseAfterScreening(
            Long surveyId,
            AnswerInsertDto.AnswerInfo answerInfo
    ) {
        boolean expected = screeningRepository.getScreeningAnswer(answerInfo.getId());

        boolean screenedFlag = (expected != answerInfo.getBooleanContent());

        Response response = responseRepository
                .findBySurveyIdAndMemberId(surveyId, answerInfo.getMemberId())
                .orElseGet(() -> Response.of(surveyId, answerInfo.getMemberId()));

        response.markScreened(screenedFlag);
        responseRepository.save(response);
    }
}
