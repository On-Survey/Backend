package OneQ.OnSurvey.domain.survey.repository.screening;

import OneQ.OnSurvey.domain.survey.entity.Screening;
import OneQ.OnSurvey.domain.survey.model.dto.ScreeningIntroData;

import java.util.List;

public interface ScreeningRepository {
    Screening getScreeningBySurveyId(Long surveyId);
    List<ScreeningIntroData> getScreeningListBySurveyIdList(List<Long> surveyIdList);

    Boolean getScreeningAnswer(Long screeningId);
    Long getSurveyId(Long screeningId);

    Screening save(Screening screening);
}
