package OneQ.OnSurvey.domain.survey.repository.screening;

import OneQ.OnSurvey.domain.survey.entity.Screening;

public interface ScreeningRepository {
    Screening getScreeningBySurveyId(Long surveyId);

    Screening save(Screening screening);
}
