package OneQ.OnSurvey.domain.survey.service;

import OneQ.OnSurvey.domain.survey.entity.Screening;
import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.model.response.SurveyFormResponse;
import OneQ.OnSurvey.domain.survey.repository.SurveyRepository;
import OneQ.OnSurvey.domain.survey.repository.screening.ScreeningRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            survey = surveyRepository.getSurveyById(surveyId);
            survey.updateSurveyTitleAndDescription(title, description);
        }
        survey = surveyRepository.save(survey);

        return SurveyFormResponse.fromEntity(survey);
    }

    @Override
    public Boolean submitSurvey(Long surveyId) {
        Survey survey = surveyRepository.getSurveyById(surveyId);
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
    public Screening upsertScreening(Long screeningId, Long surveyId, String content, Boolean answer) {
        if (screeningId == null) {
            Screening screening = Screening.of(surveyId, content, answer);

            return screeningRepository.save(screening);
        }

        Screening screening = screeningRepository.getScreeningBySurveyId(surveyId);
        screening.updateScreening(content, answer);

        return screeningRepository.save(screening);
    }
}
