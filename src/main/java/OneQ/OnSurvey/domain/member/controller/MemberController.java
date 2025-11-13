package OneQ.OnSurvey.domain.member.controller;

import OneQ.OnSurvey.domain.member.dto.MemberInfoResponse;
import OneQ.OnSurvey.domain.member.service.MemberQueryService;
import OneQ.OnSurvey.global.auth.custom.CustomUserDetails;
import OneQ.OnSurvey.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/members")
public class MemberController {

    private final MemberQueryService memberQueryService;

    @GetMapping
    @Operation(summary = "회원 정보를 조회합니다.", description = "현재 로그인한 회원의 정보(이름, 프로필 URL, 보유 코인 수)를 조회합니다.")
    public SuccessResponse<MemberInfoResponse> getMemberInfo(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return SuccessResponse.ok(memberQueryService.getMemberInfo(principal.getUserKey()));
    }
}
