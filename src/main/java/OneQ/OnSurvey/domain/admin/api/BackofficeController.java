package OneQ.OnSurvey.domain.admin.api;

import OneQ.OnSurvey.domain.admin.api.dto.request.AuthRegisterRequest;
import OneQ.OnSurvey.domain.admin.api.dto.request.AuthRequest;
import OneQ.OnSurvey.domain.admin.api.dto.response.AuthRegisterResponse;
import OneQ.OnSurvey.domain.admin.application.AdminAuthService;
import OneQ.OnSurvey.domain.admin.infra.AdminException;
import OneQ.OnSurvey.global.auth.custom.CustomAdminDetails;
import OneQ.OnSurvey.global.common.exception.CustomException;
import OneQ.OnSurvey.global.common.response.SuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping("/v1/bo")
@RequiredArgsConstructor
public class BackofficeController {

    private final AdminAuthService adminAuthService;

    private static final String ADMIN_SESSION_KEY = "ADMIN_ID";
    private static final String ADMIN_SESSION_USERNAME = "ADMIN_USERNAME";

    @PostMapping("/admin/login")
    @ResponseBody
    public SuccessResponse<Void> backofficeLogin(
        @RequestBody AuthRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
    ) {
        if (!request.validate()) {
            throw new CustomException(AdminException.BO_UNAUTHORIZED);
        }

        String adminId = adminAuthService.authenticate(request.username(), request.password());
        if (adminId == null) {
            throw new CustomException(AdminException.BO_FORBIDDEN);
        }

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(ADMIN_SESSION_KEY, adminId);
        session.setAttribute(ADMIN_SESSION_USERNAME, request.username());
        session.setMaxInactiveInterval(1800); // 30분 유지

        ResponseCookie cookie = ResponseCookie.from("JSESSIONID", session.getId())
            .path("/") // 모든 경로에서 쿠키 접근 가능하도록
            .sameSite("Lax")
            .httpOnly(true)
            .secure(false) // localhost 환경
            .maxAge(1800)
            .build();
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return SuccessResponse.ok(null);
    }

    @PostMapping("/admin/register")
    @ResponseBody
    public SuccessResponse<AuthRegisterResponse> backofficeRegister(
        @RequestBody AuthRegisterRequest request
    ) {
        if (!request.validate()) {
            throw new CustomException(AdminException.BO_REGISTER_VALIDATION);
        }

        if (!adminAuthService.register(
            request.userKey(), request.username(), request.password(), request.name())
        ) {
            throw new CustomException(AdminException.BO_REGISTER_FAILED);
        }

        return SuccessResponse.ok(new AuthRegisterResponse(request.username(), request.name()));
    }

    @GetMapping("/admin/me")
    public SuccessResponse<Void> checkSession(
        @AuthenticationPrincipal CustomAdminDetails principal,
        HttpServletRequest httpRequest
    ) {
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(ADMIN_SESSION_KEY, principal.getAdminId());
        session.setAttribute(ADMIN_SESSION_USERNAME, principal.getMemberId());
        session.setMaxInactiveInterval(1800); // 30분 유지

        return SuccessResponse.ok(null);
    }

    @GetMapping()
    public String backoffice() {
        return "bo/login";
    }

    @GetMapping("/index")
    public String index() {
        return "bo/index";
    }

    @GetMapping("/survey")
    public String survey() {
        return "bo/survey";
    }
}
