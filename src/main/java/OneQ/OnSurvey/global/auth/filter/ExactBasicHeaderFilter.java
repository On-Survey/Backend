package OneQ.OnSurvey.global.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static OneQ.OnSurvey.global.common.exception.TokenExceptionMessage.INVALID_AUTH;
import static OneQ.OnSurvey.global.common.exception.TokenExceptionMessage.INVALID_HEADER;

@RequiredArgsConstructor
@Slf4j
public class ExactBasicHeaderFilter extends OncePerRequestFilter {

    private final String expectedHeader;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String got = request.getHeader("Authorization");

        if (!StringUtils.hasText(got)) {
            log.error("[Filter 에러] : 헤더값이 비어있습니다.");
            throw new BadCredentialsException(INVALID_HEADER.getMessage());
        }

        if (!got.startsWith("Basic ")) {
            log.error("[Filter 에러] : 헤더 형식이 Basic 으로 시작하지 않습니다.");
            throw new BadCredentialsException(INVALID_HEADER.getMessage());
        }

        String base64Credentials = got.substring("Basic ".length()).trim();

        boolean okPass = MessageDigest.isEqual(
                base64Credentials.getBytes(StandardCharsets.UTF_8),
                expectedHeader.getBytes(StandardCharsets.UTF_8));

        if (!okPass) {
            log.error("[Filter 에러] : Basic Auth Header 값이 일치하지 않습니다.");
            throw new BadCredentialsException(INVALID_AUTH.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
