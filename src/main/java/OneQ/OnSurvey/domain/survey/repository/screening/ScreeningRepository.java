package OneQ.OnSurvey.domain.survey.repository.screening;

import OneQ.OnSurvey.domain.survey.entity.Screening;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.domain.survey.model.dto.ScreeningFormData;
import OneQ.OnSurvey.domain.survey.model.dto.ScreeningIntroData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ScreeningRepository {
    Screening getScreeningBySurveyId(Long surveyId);
    Slice<ScreeningIntroData> getScreeningSliceByFilters(
        Long lastSurveyId, Pageable pageable, SurveyStatus status, Long creatorId
    );
    ScreeningFormData getScreeningFormDataBySurveyId(Long surveyId);

    ScreeningIntroData getScreeningIntroDataByScreeningId(Long screeningId);
    Boolean getScreeningAnswer(Long screeningId);
    Long getSurveyId(Long screeningId);

    Screening save(Screening screening);
    void delete(Screening screening);
}
