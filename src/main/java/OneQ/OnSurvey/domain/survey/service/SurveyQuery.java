package OneQ.OnSurvey.domain.survey.service;

import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.domain.survey.model.response.*;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface SurveyQuery {
    SurveyManagementDetailResponse getSurvey(Long surveyId);
    List<SurveyManagementResponse.SurveyInfo> getSurveyListByMemberId(Long memberId);
    List<SurveyParticipationResponse.SurveyData> getParticipationSurveyList(
        Long lastSurveyId, Pageable pageable, SurveyStatus status, Long memberId
    );
    List<SurveyParticipationResponse.SurveyData> getParticipationSurveyList(
        Long lastSurveyId, LocalDateTime lastDeadline, Pageable pageable, SurveyStatus status, Long memberId
    );
    ParticipationScreeningResponse getScreeningList(
        Long lastSurveyId, Pageable pageable, Long memberId
    );
    MySurveyListResponse getMySurveys(Long memberId);
    SurveyDetailResponse getMySurveyDetail(Long memberId, Long surveyId);
}
