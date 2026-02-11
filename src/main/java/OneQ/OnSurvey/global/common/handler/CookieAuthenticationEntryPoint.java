package OneQ.OnSurvey.global.common.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CookieAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException ex) throws IOException {

        if (request.getHeader("Accept") != null
            && request.getHeader("Accept").contains("application/json")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"인증이 필요합니다.\"}");
        } else {
            // 페이지 요청인 경우 로그인 페이지로 리다이렉트
            response.sendRedirect("/v1/bo");
        }

    }
}
