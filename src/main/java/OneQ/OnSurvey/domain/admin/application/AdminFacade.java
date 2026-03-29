package OneQ.OnSurvey.domain.admin.application;

import OneQ.OnSurvey.domain.admin.api.dto.request.AdminSurveySearchQuery;
import OneQ.OnSurvey.domain.admin.api.dto.response.AdminSurveyDetailResponse;
import OneQ.OnSurvey.domain.admin.api.dto.response.AdminSurveyIntroItem;
import OneQ.OnSurvey.domain.admin.api.dto.response.SurveyGrantStatsResponse;
import OneQ.OnSurvey.domain.admin.domain.model.Admin;
import OneQ.OnSurvey.domain.admin.domain.model.AdminRole;
import OneQ.OnSurvey.domain.admin.domain.model.member.AdminMemberView;
import OneQ.OnSurvey.domain.admin.domain.model.survey.AdminSurveyListView;
import OneQ.OnSurvey.domain.admin.domain.model.survey.SurveySingleViewInfo;
import OneQ.OnSurvey.domain.admin.domain.model.survey.SurveyQuestion;
import OneQ.OnSurvey.domain.admin.domain.model.survey.SurveyScreening;
import OneQ.OnSurvey.domain.admin.domain.model.survey.SurveySection;
import OneQ.OnSurvey.domain.admin.domain.port.in.AdminUseCase;
import OneQ.OnSurvey.domain.admin.domain.port.in.AuthUseCase;
import OneQ.OnSurvey.domain.admin.domain.port.out.MemberPort;
import OneQ.OnSurvey.domain.admin.domain.port.out.SurveyPort;
import OneQ.OnSurvey.domain.admin.domain.repository.AdminRepository;
import OneQ.OnSurvey.global.promotion.port.out.PromotionGrantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminFacade implements AuthUseCase, AdminUseCase {

    private final AdminRepository adminRepository;
    private final MemberPort memberPort;
    private final SurveyPort surveyPort;
    private final PasswordEncoder passwordEncoder;
    private final PromotionGrantRepository promotionGrantRepository;

    @Override
    public String authenticate(String username, String rawPassword) {

        Admin admin = adminRepository.findByUsername(username);
        if (admin == null || !admin.matchPassword(passwordEncoder, rawPassword)) {
            return null;
        }

        return admin.getAdminId();

    }

    @Override
    @Transactional
    public boolean register(Long userKey, String username, String password, String name) {
        Admin existingAdmin = adminRepository.findByUsername(username);
        Long memberId = memberPort.validateAdminRoleAndGetMemberIdByUserKey(userKey);
        if (existingAdmin != null || memberId == null) {
            return false;
        }

        String encodedPassword = passwordEncoder.encode(password);

        Admin newAdmin = Admin.builder()
            .memberId(memberId)
            .userKey(userKey)
            .username(username)
            .password(encodedPassword)
            .name(name)
            .role(AdminRole.ROLE_ADMIN)
            .build();

        adminRepository.save(newAdmin);
        return true;
    }

    @Override
    public List<AdminMemberView> searchMembers(String email, String phoneNumber, Long memberId, String name) {
        return memberPort.searchMembers(email, phoneNumber, memberId, name);
    }

    @Override
    public Page<AdminSurveyIntroItem> getAllSurveyList(Pageable pageable, AdminSurveySearchQuery query) {
        Page<AdminSurveyListView> surveyPage = surveyPort.findPagedSurveyListByQuery(pageable, query);

        return surveyPage.map(AdminSurveyIntroItem::from);
    }

    @Override
    public AdminSurveyDetailResponse getSurveyDetail(Long surveyId) {
        SurveySingleViewInfo surveySingleViewInfo = surveyPort.findSurveyInformationById(surveyId);
        List<SurveyQuestion> questions = surveyPort.findSurveyQuestionsById(surveyId);
        SurveyScreening screening = surveyPort.findSurveyScreeningById(surveyId);
        List<SurveySection> sections = surveyPort.findSurveySectionsById(surveyId);

        return AdminSurveyDetailResponse.from(
            surveySingleViewInfo, questions, screening, sections
        );
    }

    @Override
    @Transactional
    public void changeSurveyOwner(Long surveyId, Long memberId) {
        surveyPort.updateSurveyOwner(surveyId, memberId);
    }

    @Override
    public List<SurveyGrantStatsResponse> getSurveyGrantStats() {
        return promotionGrantRepository.findSurveyGrantStats();
    }
}
