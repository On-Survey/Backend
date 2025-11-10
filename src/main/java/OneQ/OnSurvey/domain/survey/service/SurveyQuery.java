package OneQ.OnSurvey.domain.survey.service;

import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.domain.survey.model.response.SurveyManagementDetailResponse;
import OneQ.OnSurvey.domain.survey.model.response.SurveyManagementResponse;
import OneQ.OnSurvey.domain.survey.model.response.SurveyParticipationResponse;
import OneQ.OnSurvey.domain.survey.model.response.SurveyParticipationScreeningResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SurveyQuery {
    SurveyManagementDetailResponse getSurvey(Long surveyId);
    List<SurveyManagementResponse.SurveyInfo> getSurveyListByMemberId(Long memberId); // dueCount + answerCount 함께
    SurveyParticipationResponse getParticipationSurveyList(SurveyStatus status, Long lastSurveyId, Pageable pageable);
    SurveyParticipationScreeningResponse getScreeningList(Long lastSurveyId, Pageable pageable);
}
