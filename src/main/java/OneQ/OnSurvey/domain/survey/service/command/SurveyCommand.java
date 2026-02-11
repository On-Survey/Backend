package OneQ.OnSurvey.domain.survey.service.command;

import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.survey.model.dto.SurveyOwnerChangeDto;
import OneQ.OnSurvey.domain.survey.model.request.FreeSurveyFormRequest;
import OneQ.OnSurvey.domain.survey.model.request.SurveyFormCreateRequest;
import OneQ.OnSurvey.domain.survey.model.request.SurveyFormRequest;
import OneQ.OnSurvey.domain.survey.model.response.InterestResponse;
import OneQ.OnSurvey.domain.survey.model.response.ScreeningResponse;
import OneQ.OnSurvey.domain.survey.model.response.SurveyFormResponse;

import java.util.Set;

public interface SurveyCommand {
    SurveyFormResponse upsertSurvey(Long memberId, Long surveyId, SurveyFormCreateRequest request);
    SurveyFormResponse submitSurvey(Long userKey, Long surveyId, SurveyFormRequest request);
    SurveyFormResponse submitFreeSurvey(Long userKey, Long surveyId, FreeSurveyFormRequest request);

    ScreeningResponse upsertScreening(Long surveyId, String content, Boolean answer);
    Boolean refundSurvey(Long memberId, Long surveyId);

    InterestResponse upsertInterest(Long surveyId, Set<Interest> interestSet);

    boolean sendSurveyHeartbeat(Long surveyId, Long userKey);
    void updateSurveyOwner(SurveyOwnerChangeDto changeDto);
}
