package OneQ.OnSurvey.global.infra.toss.service;

import OneQ.OnSurvey.domain.member.service.MemberService;
import OneQ.OnSurvey.domain.member.value.Role;
import OneQ.OnSurvey.global.auth.token.TokenStore;
import OneQ.OnSurvey.global.auth.token.service.BlackListService;
import OneQ.OnSurvey.global.auth.utils.JWTUtil;
import OneQ.OnSurvey.global.exception.CustomException;
import OneQ.OnSurvey.global.infra.toss.adapter.TossApiClient;
import OneQ.OnSurvey.global.infra.toss.dto.LoginMeResponse;
import OneQ.OnSurvey.global.infra.toss.dto.ReissueRequest;
import OneQ.OnSurvey.global.infra.toss.dto.TossLoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import java.time.Duration;

import static OneQ.OnSurvey.global.auth.AuthErrorCode.INVALID_REFRESH_TOKEN;
import static OneQ.OnSurvey.global.infra.toss.TossErrorCode.TOSS_API_CONNECTION_ERROR;

@Service
@RequiredArgsConstructor
@Slf4j
public class TossAuthService {

    @Value("${toss.secret.private-key}")
    private String privateKey;

    @Value("${toss.secret.public-crt}")
    private String publicCrt;

    @Value("${spring.jwt.iss}")
    private String tokenIss;

    @Value("${spring.token.access.category}")
    private String accessTokenCategory;

    @Value("${spring.token.refresh.category}")
    private String refreshTokenCategory;

    @Value("${spring.token.refresh.expire-ms}")
    private Long refreshTokenExpireMs;

    private final TossApiClient tossApiClient;
    private final MemberService memberService;
    private final JWTUtil jwtUtil;
    private final TokenStore tokenStore;
    private final BlackListService blacklistService;

    public Boolean createAccessAndRefreshToken(TossLoginRequest tossLoginRequest, HttpServletResponse response) {

        // 토스 액세스 토큰 발급
        LoginMeResponse.Success loginMeResponse;
        try {
            SSLContext context = tossApiClient.createSSLContext(publicCrt, privateKey);
            String tossAccessToken = tossApiClient.getAccessToken(context, tossLoginRequest);
            loginMeResponse = tossApiClient.getLoginMe(context, tossAccessToken);
        } catch (Exception e) {
            log.error("[TossAuthService Error] : Toss Access Token 발급 중 오류가 발생했습니다.", e);
            throw new CustomException(TOSS_API_CONNECTION_ERROR);
        }

        memberService.upsertMember(loginMeResponse);

        String accessToken = jwtUtil.createJWT(accessTokenCategory, loginMeResponse.userKey(),
                Role.ROLE_MEMBER.name());
        String refreshToken = jwtUtil.createJWT(refreshTokenCategory, loginMeResponse.userKey(),
                Role.ROLE_MEMBER.name());

        tokenStore.saveRefreshToken(loginMeResponse.userKey(), refreshToken, Duration.ofMillis(refreshTokenExpireMs));

        response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        response.setHeader("X-Refresh-Token", "Bearer " + refreshToken);

        return true;
    }

    public Boolean reissueToken(ReissueRequest reissueRequest, HttpServletResponse response) {
        String presentedRt = reissueRequest.refreshToken();

        if (presentedRt == null || presentedRt.isBlank()) {
            return false;
        }

        try {
            jwtUtil.isExpired(presentedRt);
            jwtUtil.validateIssuer(presentedRt, tokenIss);

            // 토큰이 refresh 인지 확인 (발급시 페이로드에 명시)
            String category = jwtUtil.getClaimFromToken(presentedRt, "category", String.class);
            if (category == null || !category.equals(refreshTokenCategory)) {
                throw new BadCredentialsException(INVALID_REFRESH_TOKEN.getMessage());
            }

            Long userKey = jwtUtil.getUserKeyFromSubject(presentedRt);

            // 서버 저장 해시와 대조 (재사용/탈취 감지)
            String savedRefresh = tokenStore.getRefreshToken(userKey)
                    .orElseThrow(() -> new BadCredentialsException(INVALID_REFRESH_TOKEN.getMessage()));

            if (!presentedRt.equals(savedRefresh)) {
                tokenStore.deleteRefresh(userKey);
                throw new BadCredentialsException(INVALID_REFRESH_TOKEN.getMessage());
            }

            String newAt = jwtUtil.createJWT(accessTokenCategory, userKey,
                    Role.ROLE_MEMBER.name());
            String newRt = jwtUtil.createJWT(refreshTokenCategory, userKey,
                    Role.ROLE_MEMBER.name());

            tokenStore.saveRefreshToken(userKey, newRt, Duration.ofMillis(refreshTokenExpireMs));

            response.setHeader("X-Refresh-Token", "Bearer " + newRt);
            response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + newAt);
            return true;

        } catch (JwtException | IllegalArgumentException | BadCredentialsException e) {
            throw new CustomException(INVALID_REFRESH_TOKEN);
        }
    }

    public Boolean logout(HttpServletRequest request) {
        String accessToken = jwtUtil.resolveToken(request);
        jwtUtil.isExpired(accessToken);
        jwtUtil.validateIssuer(accessToken, tokenIss);
        Long userKey = jwtUtil.getUserKeyFromSubject(accessToken);

        long ttlSeconds = Math.max(0,
                (jwtUtil.getExpiration(accessToken).getTime() - System.currentTimeMillis()) / 1000L);
        if (ttlSeconds > 0) {
            String key = jwtUtil.getJti(accessToken);
            blacklistService.blacklist(key, ttlSeconds);
        }
        tokenStore.deleteRefresh(userKey);
        return true;
    }
}
