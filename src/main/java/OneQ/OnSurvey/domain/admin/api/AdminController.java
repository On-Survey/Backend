package OneQ.OnSurvey.domain.admin.api;

import OneQ.OnSurvey.domain.admin.api.dto.request.AdminSurveySearchQuery;
import OneQ.OnSurvey.domain.admin.api.dto.request.ChangeSurveyOwnerRequest;
import OneQ.OnSurvey.domain.admin.api.dto.response.AdminSurveyDetailResponse;
import OneQ.OnSurvey.domain.admin.api.dto.response.MemberSearchResponse;
import OneQ.OnSurvey.domain.admin.api.dto.response.AdminSurveyIntroItem;
import OneQ.OnSurvey.domain.admin.api.dto.response.SurveyGrantStatsResponse;
import OneQ.OnSurvey.domain.admin.application.AdminFacade;
import OneQ.OnSurvey.domain.admin.domain.model.member.AdminMemberView;
import OneQ.OnSurvey.global.common.response.PageResponse;
import OneQ.OnSurvey.global.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminFacade adminFacade;

    @GetMapping("/search")
    @Operation(summary = "회원 검색", description = "이메일, 전화번호, 회원ID, 이름으로 회원을 검색합니다.")
    public SuccessResponse<MemberSearchResponse> searchMembers(
        @RequestParam(required = false) String email,
        @RequestParam(required = false) String phoneNumber,
        @RequestParam(required = false) Long memberId,
        @RequestParam(required = false) String name
    ) {
        log.info("[ADMIN] 회원 검색 - email: {}, phone: {}, memberId: {}, name: {}", email, phoneNumber, memberId, name);
        List<AdminMemberView> members = adminFacade.searchMembers(email, phoneNumber, memberId, name);
        return SuccessResponse.ok(MemberSearchResponse.from(members));
    }

    @GetMapping("/surveys")
    @Operation(summary = "설문 목록 조회 (어드민)", description = "어드민 권한으로 설문 목록을 조회합니다.")
    public PageResponse<AdminSurveyIntroItem> getAllSurveyList(
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        @ModelAttribute AdminSurveySearchQuery query
        ) {
        log.info("[ADMIN] 설문 목록 조회 - query: {}, pageable: {}", query, pageable);
        return PageResponse.ok(adminFacade.getAllSurveyList(pageable, query));
    }

    @GetMapping("/surveys/{surveyId}")
    @Operation(summary = "특정 설문 조회 (어드민)", description = "어드민 권한으로 설문을 조회합니다.")
    public SuccessResponse<AdminSurveyDetailResponse> getQuestionsCompleted(
        @PathVariable Long surveyId
    ) {
        log.info("[ADMIN] 특정 설문 조회 - surveyId: {}", surveyId);

        return SuccessResponse.ok(adminFacade.getSurveyDetail(surveyId));
    }

    @GetMapping("/promotion-grants/survey-stats")
    @Operation(summary = "설문별 리워드 지급 현황 조회", description = "설문 단위로 성공/실패/대기 건수를 집계하여 최신순으로 반환합니다.")
    public SuccessResponse<List<SurveyGrantStatsResponse>> getSurveyGrantStats() {
        return SuccessResponse.ok(adminFacade.getSurveyGrantStats());
    }

    @PatchMapping("/surveys/{surveyId}/owner")
    @Operation(summary = "설문 소유자 변경 (어드민)", description = "어드민 권한으로 설문의 소유자를 변경합니다.")
    public SuccessResponse<String> changeSurveyOwner(
        @PathVariable Long surveyId,
        @RequestBody ChangeSurveyOwnerRequest request
    ) {
        log.info("[ADMIN] 설문 소유자 변경 요청 - surveyId: {}, newMemberId: {}", surveyId, request.newMemberId());

        adminFacade.changeSurveyOwner(surveyId, request.newMemberId());

        return SuccessResponse.ok("설문 소유자가 변경되었습니다.");
    }
}
