package OneQ.OnSurvey.domain.member.controller;

import OneQ.OnSurvey.domain.member.dto.MemberInfoResponse;
import OneQ.OnSurvey.domain.member.dto.OnboardingRequest;
import OneQ.OnSurvey.domain.member.dto.ProfileImageUpdateRequest;
import OneQ.OnSurvey.domain.member.service.MemberFinder;
import OneQ.OnSurvey.domain.member.service.MemberUpdater;
import OneQ.OnSurvey.global.auth.custom.CustomUserDetails;
import OneQ.OnSurvey.global.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/members")
public class MemberController {

    private final MemberFinder memberFinder;
    private final MemberUpdater memberUpdater;

    @GetMapping
    @Operation(summary = "회원 정보를 조회합니다.", description = "현재 로그인한 회원의 정보(이름, 프로필 URL, 보유 코인 수)를 조회합니다.")
    public SuccessResponse<MemberInfoResponse> getMemberInfo(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return SuccessResponse.ok(memberFinder.getMemberInfo(principal.getUserKey()));
    }

    @PatchMapping("/profile-image")
    @Operation(summary = "프로필 이미지 설정", description = "프로필 이미지 URL을 설정합니다.")
    public SuccessResponse<String> changeProfileImage(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody ProfileImageUpdateRequest request
    ) {
        memberUpdater.changeProfileImage(principal.getUserKey(), request.profileUrl());
        return SuccessResponse.ok("프로필 이미지가 변경되었습니다.");
    }

    @PatchMapping("/onboarding")
    @Operation(summary = "온보딩 정보 설정", description = "거주지와 관심사를 등록하고 온보딩을 완료합니다.")
    public SuccessResponse<String> completeOnboarding(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody OnboardingRequest request
    ) {
        memberUpdater.completeOnboarding(
                principal.getUserKey(),
                request.residence(),
                request.interests()
        );
        return SuccessResponse.ok("온보딩이 완료되었습니다.");
    }
}
