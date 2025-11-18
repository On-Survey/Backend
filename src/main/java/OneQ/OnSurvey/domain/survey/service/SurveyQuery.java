package OneQ.OnSurvey.domain.survey.service;

import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.domain.survey.model.response.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SurveyQuery {
    SurveyManagementDetailResponse getSurvey(Long surveyId);
    List<SurveyManagementResponse.SurveyInfo> getSurveyListByMemberId(Long memberId);
    SurveyParticipationResponse getParticipationSurveyList(SurveyStatus status, Long lastSurveyId, Pageable pageable, Long memberId);
    ParticipationScreeningResponse getScreeningList(Long lastSurveyId, Pageable pageable, Long memberId);
    MySurveyListResponse getMySurveys(Long memberId);
    SurveyDetailResponse getMySurveyDetail(Long memberId, Long surveyId);
}
