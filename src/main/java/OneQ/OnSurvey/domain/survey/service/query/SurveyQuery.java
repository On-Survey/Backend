package OneQ.OnSurvey.domain.survey.service.query;

import OneQ.OnSurvey.domain.question.model.dto.SectionDto;
import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.domain.survey.model.dto.ScreeningViewData;
import OneQ.OnSurvey.domain.survey.model.dto.SurveyDetailData;
import OneQ.OnSurvey.domain.survey.model.dto.SurveyListView;
import OneQ.OnSurvey.domain.survey.model.dto.SurveySearchQuery;
import OneQ.OnSurvey.domain.survey.model.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface SurveyQuery {
    SurveyManagementDetailResponse getSurvey(Long surveyId);
    List<SurveyManagementResponse.SurveyInformation> getSurveyListByMemberId(Long memberId);
    SurveyParticipationResponse getParticipationSurveySlice(
        Long lastSurveyId, Pageable pageable, SurveyStatus status, Long memberId, Long userKey
    );

    SurveyParticipationResponse.SliceSurveyData getParticipationSurveyList(
        Long lastSurveyId, Pageable pageable, SurveyStatus status, Long memberId, Long userKey
    );
    SurveyParticipationResponse.SliceSurveyData getParticipationSurveyList(
        Long lastSurveyId, LocalDateTime lastDeadline, Pageable pageable, SurveyStatus status, Long memberId, Long userKey
    );
    ParticipationScreeningListResponse getScreeningList(
        Long lastSurveyId, Pageable pageable, Long memberId, Long userKey
    );
    ParticipationScreeningSingleResponse getScreeningSingleResponse(Long screeningId);
    ParticipationInfoResponse getParticipationInfo(Long surveyId, Long userKey, Long memberId);
    ParticipationQuestionResponse getParticipationQuestionInfo(Long surveyId, Integer section, Long userKey);

    MySurveyListResponse getMySurveys(Long memberId);
    SurveyDetailResponse getMySurveyDetail(Long memberId, Long surveyId);

    void validateSurveyRequest(Long surveyId, Long memberId, SurveyStatus status);
    boolean checkValidSegmentation(Long surveyId, Long userKey);

    Survey getSurveyById(Long surveyId);
    Integer getPromotionAmountBySurveyId(Long surveyId);

    // 외부 PORT
    Page<SurveyListView> getPagedSurveyListViewByQuery(Pageable pageable, SurveySearchQuery query);
    SurveyDetailData getSurveyDetailById(Long surveyId);
    ScreeningViewData getScreeningIntroBySurveyId(Long surveyId);
    List<SectionDto> getSectionDtoListBySurveyId(Long surveyId);
}
