package OneQ.OnSurvey.global.auth.filter;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.repository.MemberRepository;
import OneQ.OnSurvey.domain.member.value.MemberStatus;
import OneQ.OnSurvey.domain.member.value.Role;
import OneQ.OnSurvey.global.auth.custom.CustomUserDetails;
import OneQ.OnSurvey.global.auth.token.service.BlackListService;
import OneQ.OnSurvey.global.auth.utils.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static OneQ.OnSurvey.domain.member.MemberErrorCode.MEMBER_NOT_FOUND;
import static OneQ.OnSurvey.global.exception.TokenExceptionMessage.INVALID_TOKEN;
import static OneQ.OnSurvey.global.exception.TokenExceptionMessage.SESSION_EXPIRATION;

@RequiredArgsConstructor
@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final String tokenIss;
    private final JWTUtil jwtUtil;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final BlackListService blackListService;
    private final MemberRepository memberRepository;
    private final String accessTokenCategory;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try{
            // Request 에서 access token 추출
            String accessToken = jwtUtil.resolveToken(request);

            // 토큰이 없다면 다음 필터로 넘김
            if (accessToken == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 토큰 만료 여부 확인, 만료시 다음 필터로 넘기지 않음
            jwtUtil.isExpired(accessToken);

            // 이슈어 검증
            jwtUtil.validateIssuer(accessToken, tokenIss);

            // 토큰이 access인지 확인 (발급시 페이로드에 명시)
            String category = jwtUtil.getClaimFromToken(accessToken, "category", String.class);

            // blackList 검증
            String jti = jwtUtil.getJti(accessToken);
            if(blackListService.isBlacklisted(jti)) {
                log.error("[Filter 에러] : 사용할 수 없는 토큰입니다.");
                throw new BadCredentialsException(INVALID_TOKEN.getMessage());
            }
            if (category == null || !category.equals(accessTokenCategory)) {
                log.error("[Filter 에러] : Access Token이 아닙니다.");
                throw new BadCredentialsException(INVALID_TOKEN.getMessage());
            }

            Long userKey = jwtUtil.getUserKeyFromSubject(accessToken);
            String roleName = jwtUtil.getClaimFromToken(accessToken, "role", String.class);
            Role role = Role.valueOf(roleName);

            Member member = memberRepository.findMemberByUserKey(userKey)
                    .orElseThrow(() -> new BadCredentialsException(MEMBER_NOT_FOUND.getMessage()));

            // 토스 로그인 세션이 만료되었을 때
            if(!MemberStatus.isSameMemberStatus(member.getStatus(), MemberStatus.ACTIVE)) {
                log.error("[Filter 에러] : 세션이 만료된 유저입니다.");
                long ttlSeconds = Math.max(0,
                        (jwtUtil.getExpiration(accessToken).getTime() - System.currentTimeMillis()) / 1000L);
                blackListService.blacklist(jti, ttlSeconds);
                throw new BadCredentialsException(SESSION_EXPIRATION.getMessage());
            }

            Member memberEntity = Member.builder()
                    .userKey(userKey)
                    .role(role)
                    .build();

            CustomUserDetails customUserDetails = new CustomUserDetails(memberEntity);
            Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);
            filterChain.doFilter(request, response);
        } catch (AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, ex);
        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(
                    request, response,
                    new BadCredentialsException(INVALID_TOKEN.getMessage(), ex)
            );
        }
    }
}
