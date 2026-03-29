package OneQ.OnSurvey.domain.admin.domain.port.in;

import OneQ.OnSurvey.domain.admin.api.dto.request.AdminSurveySearchQuery;
import OneQ.OnSurvey.domain.admin.api.dto.response.AdminSurveyDetailResponse;
import OneQ.OnSurvey.domain.admin.api.dto.response.AdminSurveyIntroItem;
import OneQ.OnSurvey.domain.admin.api.dto.response.SurveyGrantStatsResponse;
import OneQ.OnSurvey.domain.admin.domain.model.member.AdminMemberView;
import OneQ.OnSurvey.domain.admin.domain.model.survey.OngoingSurveyView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminUseCase {

    List<AdminMemberView> searchMembers(String email, String phoneNumber, Long memberId, String name);

    Page<AdminSurveyIntroItem> getAllSurveyList(Pageable pageable, AdminSurveySearchQuery query);

    AdminSurveyDetailResponse getSurveyDetail(Long surveyId);

    void changeSurveyOwner(Long surveyId, Long newMemberId);

    List<SurveyGrantStatsResponse> getSurveyGrantStats();

    List<OngoingSurveyView> getOngoingSurveys();
}
