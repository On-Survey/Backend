package OneQ.OnSurvey.global.auth.filter;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.repository.MemberRepository;
import OneQ.OnSurvey.domain.member.value.MemberStatus;
import OneQ.OnSurvey.global.auth.application.AuthUseCase;
import OneQ.OnSurvey.global.auth.custom.CustomUserDetails;
import OneQ.OnSurvey.global.common.exception.CustomException;
import OneQ.OnSurvey.global.infra.toss.common.dto.auth.LoginMeResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class TossAuthFilter extends OncePerRequestFilter {

    private final AuthUseCase authUseCase;
    private final MemberRepository memberRepository;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String path = req.getRequestURI();
        String ctx  = req.getContextPath();
        if (ctx != null && !ctx.isEmpty() && path.startsWith(ctx)) {
            path = path.substring(ctx.length());
        }

        return "OPTIONS".equalsIgnoreCase(req.getMethod())
                || path.startsWith("/auth/")
                || path.equals("/connect-out")
                || path.startsWith("/public/")
                || path.equals("/actuator/health")
                || path.startsWith("/actuator/health/")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/test/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        try {
            LoginMeResponse.Success me = authUseCase.authenticateWithToss(req);

            Member member = memberRepository.findMemberByUserKey(me.userKey())
                    .orElseThrow(() -> new BadCredentialsException("member not found"));

            if (!MemberStatus.isSameMemberStatus(member.getStatus(), MemberStatus.ACTIVE)) {
                throw new BadCredentialsException("session expired");
            }

            CustomUserDetails principal = new CustomUserDetails(
                    Member.builder().userKey(me.userKey()).role(member.getRole()).build()
            );
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            chain.doFilter(req, res);
        } catch (AuthenticationException | CustomException e) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(req, res, new BadCredentialsException("invalid token", e));
        }
    }
}

