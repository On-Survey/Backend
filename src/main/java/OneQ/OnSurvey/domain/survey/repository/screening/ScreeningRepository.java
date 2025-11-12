package OneQ.OnSurvey.domain.survey.repository.screening;

import OneQ.OnSurvey.domain.survey.entity.Screening;

import java.util.List;

public interface ScreeningRepository {
    Screening getScreeningBySurveyId(Long surveyId);
    List<Screening> getScreeningListBySurveyIdList(List<Long> surveyId);

    Screening save(Screening screening);
}
