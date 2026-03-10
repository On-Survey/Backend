package OneQ.OnSurvey.domain.survey.repository;

import OneQ.OnSurvey.domain.member.dto.MemberSegmentation;
import OneQ.OnSurvey.domain.participation.model.dto.ParticipationStatus;
import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.domain.survey.model.dto.SurveyDetailData;
import OneQ.OnSurvey.domain.survey.model.dto.SurveyListView;
import OneQ.OnSurvey.domain.survey.model.dto.SurveySearchQuery;
import OneQ.OnSurvey.domain.survey.model.dto.SurveyWithEligibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SurveyRepository {
    Optional<Survey> getSurveyById(Long surveyId);
    List<Survey> getSurveyListByMemberId(Long memberId);
    List<Long> getSurveyIdListByFilters(
        Long lastSurveyId, LocalDateTime lastDeadline, Pageable pageable,
        SurveyStatus status, Long creatorId, Collection<Long> excludedIds);
    Page<SurveyListView> getPagedSurveyListViewByQuery(Pageable pageable, SurveySearchQuery query);
    SurveyDetailData getSurveyDetailDataById(Long surveyId);
    Slice<SurveyWithEligibility> getSurveyListWithEligibility(
        Long lastSurveyId, LocalDateTime lastDeadline, Pageable pageable,
        SurveyStatus status, Long creatorId, Collection<Long> excludedIds, MemberSegmentation memberSegmentation);

    Survey save(Survey survey);

    SurveyStatus getSurveyStatusById(Long surveyId);
    ParticipationStatus getParticipationStatus(Long surveyId, Long memberId);
}
