package OneQ.OnSurvey.global.common.handler;

import OneQ.OnSurvey.global.common.exception.ErrorCode;
import OneQ.OnSurvey.global.common.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException ex) throws IOException {

        if (response.isCommitted()) return;

        ErrorResponse<Object> body = ErrorResponse.of(
                ErrorCode.UNAUTHORIZED.getErrorCode(),
                ErrorCode.UNAUTHORIZED.getMessage()
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");

        objectMapper.writeValue(response.getWriter(), body);
    }
}
