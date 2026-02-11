package OneQ.OnSurvey.domain.survey.repository.screening;

import OneQ.OnSurvey.domain.survey.entity.Screening;
import OneQ.OnSurvey.domain.survey.model.dto.ScreeningFormData;
import OneQ.OnSurvey.domain.survey.model.dto.ScreeningIntroData;
import OneQ.OnSurvey.domain.survey.model.dto.ScreeningViewData;

import java.util.List;

public interface ScreeningRepository {
    Screening getScreeningBySurveyId(Long surveyId);
    List<ScreeningIntroData> getScreeningListBySurveyIdList(List<Long> surveyIdList);
    ScreeningFormData getScreeningFormDataBySurveyId(Long surveyId);
    ScreeningViewData getScreeningIntroBySurveyId(Long surveyId);

    ScreeningIntroData getScreeningIntroDataByScreeningId(Long screeningId);
    Boolean getScreeningAnswer(Long screeningId);
    Long getSurveyId(Long screeningId);

    Screening save(Screening screening);
    void delete(Screening screening);
}
