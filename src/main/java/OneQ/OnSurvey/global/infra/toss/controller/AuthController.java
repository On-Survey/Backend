package OneQ.OnSurvey.global.infra.toss.controller;

import OneQ.OnSurvey.global.infra.toss.dto.ReissueRequest;
import OneQ.OnSurvey.global.infra.toss.dto.TossLoginRequest;
import OneQ.OnSurvey.global.infra.toss.service.TossAuthService;
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
    @Operation(summary = "토스 로그인", description = "login")
    public ResponseEntity<Boolean> tossLogin(
            HttpServletResponse httpServletResponse,
            @RequestBody TossLoginRequest tossLoginRequest
    ) {
        return ResponseEntity.ok(tossAuthService.createAccessAndRefreshToken(tossLoginRequest, httpServletResponse));
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/reissue")
    @Operation(summary = "토큰 재발급", description = "reissue")
    public ResponseEntity<Boolean> reissue(
            ReissueRequest reissueRequest,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok(tossAuthService.reissueToken(reissueRequest, response));
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "logout")
    public ResponseEntity<Boolean> logout(
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(tossAuthService.logout(request));
    }
}
