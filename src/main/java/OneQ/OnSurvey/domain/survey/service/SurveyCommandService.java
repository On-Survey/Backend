package OneQ.OnSurvey.domain.survey.service;

import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.survey.entity.Screening;
import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.model.response.InterestResponse;
import OneQ.OnSurvey.domain.survey.model.response.ScreeningResponse;
import OneQ.OnSurvey.domain.survey.model.response.SurveyFormResponse;
import OneQ.OnSurvey.domain.survey.repository.SurveyRepository;
import OneQ.OnSurvey.domain.survey.repository.screening.ScreeningRepository;
import OneQ.OnSurvey.global.exception.CustomException;
import OneQ.OnSurvey.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class SurveyCommandService implements SurveyCommand {
    private final SurveyRepository surveyRepository;
    private final ScreeningRepository screeningRepository;

    @Override
    public SurveyFormResponse upsertSurvey(Long surveyId, String title, String description, Long memberId) {
        Survey survey;
        if (surveyId == null) {
            survey = Survey.of(
                memberId,
                title,
                description
            );
        } else {
            survey = surveyRepository.getSurveyById(surveyId).orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));
            survey.updateSurveyTitleAndDescription(title, description);
        }
        survey = surveyRepository.save(survey);
      
        return SurveyFormResponse.fromEntity(survey);
    }

    @Override
    public Boolean submitSurvey(Long surveyId) {
        Survey survey = surveyRepository.getSurveyById(surveyId).orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));
        survey.submitSurvey();

        surveyRepository.save(survey);

        return true;
    }

    @Override
    public Boolean deleteById(Long surveyId) {
        surveyRepository.deleteById(surveyId);
        return true;
    }

    @Override
    public ScreeningResponse upsertScreening(Long screeningId, Long surveyId, String content, Boolean answer) {
        Screening screening;
        if (screeningId == null) {
            screening = Screening.of(surveyId, content, answer);
            screening = screeningRepository.save(screening);
        } else {
            screening = screeningRepository.getScreeningBySurveyId(surveyId);
            screening.updateScreening(content, answer);

            screeningRepository.save(screening);
        }
        return ScreeningResponse.builder()
            .screeningId(screening.getId())
            .surveyId(screening.getSurveyId())
            .content(screening.getContent())
            .answer(answer)
            .build();
    }

    @Override
    public InterestResponse upsertInterest(Long surveyId, Set<Interest> interestSet) {
        Survey survey = surveyRepository.getSurveyById(surveyId).orElseThrow(
            () -> new CustomException(ErrorCode.INVALID_REQUEST)
        );
        survey.updateInterests(interestSet);

        survey = surveyRepository.save(survey);

        return InterestResponse.builder()
            .interests(survey.getInterests())
            .build();
    }
}
