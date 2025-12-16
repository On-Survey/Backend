package OneQ.OnSurvey.domain.survey.service.query;

import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.domain.survey.model.response.*;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface SurveyQuery {
    SurveyManagementDetailResponse getSurvey(Long surveyId);
    List<SurveyManagementResponse.SurveyInformation> getSurveyListByMemberId(Long memberId);
    SurveyParticipationResponse.SliceSurveyData getParticipationSurveyList(
        Long lastSurveyId, Pageable pageable, SurveyStatus status, Long memberId
    );
    SurveyParticipationResponse.SliceSurveyData getParticipationSurveyList(
        Long lastSurveyId, LocalDateTime lastDeadline, Pageable pageable, SurveyStatus status, Long memberId
    );
    ParticipationScreeningResponse getScreeningList(
        Long lastSurveyId, Pageable pageable, Long memberId
    );
    MySurveyListResponse getMySurveys(Long memberId);
    SurveyDetailResponse getMySurveyDetail(Long memberId, Long surveyId);

    void validateSurveyRequest(Long surveyId, Long memberId, SurveyStatus status);
    Survey getSurveyById(Long surveyId);
    boolean checkValidSegmentation(Long surveyId, Long userKey);
}
