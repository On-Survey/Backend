package OneQ.OnSurvey.global.infra.toss.auth.controller;

import OneQ.OnSurvey.global.infra.toss.auth.dto.TossLoginRequest;
import OneQ.OnSurvey.global.infra.toss.auth.dto.TossLoginResponse;
import OneQ.OnSurvey.global.infra.toss.auth.dto.TossReissueRequest;
import OneQ.OnSurvey.global.infra.toss.auth.service.TossAuthService;
import OneQ.OnSurvey.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final TossAuthService tossAuthService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/toss/login")
    @Operation(summary = "토스 로그인", description = "인가코드와 referrer을 받아 토큰을 헤더(Authorization, X-Refresh-Token)로 전달합니다.")
    public ResponseEntity<SuccessResponse<TossLoginResponse>> tossLogin(
            HttpServletResponse httpServletResponse,
            @RequestBody TossLoginRequest tossLoginRequest
    ) {
        TossLoginResponse result = tossAuthService.createAccessAndRefreshToken(tossLoginRequest, httpServletResponse);
        return ResponseEntity.ok(SuccessResponse.ok(result));
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/reissue")
    @Operation(summary = "토큰 재발급", description = "Refresh Token을 받아 유효한 경우 Access Token을 재발급합니다.")
    public ResponseEntity<SuccessResponse<Boolean>> reissue(
            HttpServletResponse response,
            @RequestBody TossReissueRequest reissueRequest
    ) {
        return ResponseEntity.ok(SuccessResponse.ok(tossAuthService.reissueToken(reissueRequest, response)));
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그아웃 처리 및 RT 삭제 및 AT를 블랙리스트에 등록합니다.")
    public ResponseEntity<SuccessResponse<Boolean>> logout(
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(SuccessResponse.ok(tossAuthService.logoutByAccessToken(request)));
    }
}
