package OneQ.OnSurvey.global.auth.application;

import OneQ.OnSurvey.domain.admin.domain.port.in.AuthUseCase;
import OneQ.OnSurvey.domain.admin.infra.AdminException;
import OneQ.OnSurvey.global.auth.dto.AdminLoginRequest;
import OneQ.OnSurvey.global.auth.dto.AdminLoginResult;
import OneQ.OnSurvey.global.auth.dto.AdminRegisterRequest;
import OneQ.OnSurvey.global.auth.dto.AdminSessionInfo;
import OneQ.OnSurvey.global.auth.filter.SessionLoginRateLimiter;
import OneQ.OnSurvey.global.common.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminSessionFacade implements AdminSessionUseCase {

    @Value("${app.cookie.secure:true}")
    private boolean isSecure;

    private final AuthUseCase adminAuthUseCase;
    private final SessionLoginRateLimiter rateLimiter;

    private static final String ADMIN_SESSION_KEY = "ADMIN_ID";
    private static final String ADMIN_SESSION_USERNAME = "ADMIN_USERNAME";
    private static final int SESSION_MAX_INACTIVE_INTERVAL = 1800;

    @Override
    public AdminLoginResult login(
        AdminLoginRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
    ) {
        String clientIp = getClientIp(httpRequest);
        if (!rateLimiter.tryConsume(clientIp)) {
            log.warn("[BO Login] Rate limit 초과 - IP: {}", clientIp);
            throw new CustomException(AdminException.BO_TOO_MANY_REQUEST);
        }
        if (!request.validate()) {
            throw new CustomException(AdminException.BO_UNAUTHORIZED);
        }
        String adminId = adminAuthUseCase.authenticate(request.username(), request.password());
        if (adminId == null) {
            log.warn("[BO Login] 백오피스 로그인 시도 실패 - username: {}, IP: {}", request.username(), clientIp);
            throw new CustomException(AdminException.BO_FORBIDDEN);
        }
        createSessionAndSetCookie(adminId, request.username(), httpRequest, httpResponse);
        log.info("[BO Login] Success - username: {}, IP: {}", request.username(), clientIp);
        return AdminLoginResult.success(request.username());
    }

    @Override
    public boolean register(AdminRegisterRequest request) {
        if (!request.validate()) {
            throw new CustomException(AdminException.BO_REGISTER_VALIDATION);
        }
        if (!adminAuthUseCase.register(request.userKey(), request.username(), request.password(), request.name())) {
            throw new CustomException(AdminException.BO_REGISTER_FAILED);
        }
        return true;
    }

    @Override
    public AdminSessionInfo refreshSession(String adminId, String username, HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(ADMIN_SESSION_KEY, adminId);
        session.setAttribute(ADMIN_SESSION_USERNAME, username);
        session.setMaxInactiveInterval(SESSION_MAX_INACTIVE_INTERVAL);
        return AdminSessionInfo.of(username);
    }

    private void createSessionAndSetCookie(String adminId, String username, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        HttpSession oldSession = httpRequest.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(ADMIN_SESSION_KEY, adminId);
        session.setAttribute(ADMIN_SESSION_USERNAME, username);
        session.setMaxInactiveInterval(SESSION_MAX_INACTIVE_INTERVAL);
        ResponseCookie cookie = ResponseCookie.from("JSESSIONID", session.getId())
            .path("/")
            .sameSite("Lax")
            .httpOnly(true)
            .secure(isSecure)
            .maxAge(SESSION_MAX_INACTIVE_INTERVAL)
            .build();
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
