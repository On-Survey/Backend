package OneQ.OnSurvey.global.infra.toss.auth.service;

import OneQ.OnSurvey.domain.member.service.MemberModifyService;
import OneQ.OnSurvey.domain.member.value.Role;
import OneQ.OnSurvey.global.auth.token.TokenStore;
import OneQ.OnSurvey.global.auth.token.service.BlackListService;
import OneQ.OnSurvey.global.auth.utils.JWTUtil;
import OneQ.OnSurvey.global.exception.CustomException;
import OneQ.OnSurvey.global.infra.toss.adapter.TossApiClient;
import OneQ.OnSurvey.global.infra.toss.auth.dto.DecryptedLoginMeResponse;
import OneQ.OnSurvey.global.infra.toss.auth.dto.LoginMeResponse;
import OneQ.OnSurvey.global.infra.toss.auth.dto.ReissueRequest;
import OneQ.OnSurvey.global.infra.toss.auth.dto.TossLoginRequest;
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
import static OneQ.OnSurvey.global.infra.toss.common.TossErrorCode.TOSS_API_CONNECTION_ERROR;
import static OneQ.OnSurvey.global.infra.toss.common.TossErrorCode.TOSS_DECRYPT_ERROR;

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
    private final MemberModifyService memberModifyService;
    private final JWTUtil jwtUtil;
    private final TokenStore tokenStore;
    private final BlackListService blacklistService;
    private final TossMemberInfoDecryptService tossMemberInfoDecryptService;

    public Boolean createAccessAndRefreshToken(TossLoginRequest tossLoginRequest, HttpServletResponse response) {

        LoginMeResponse.Success loginMeResponse = getTossUserInfo(tossLoginRequest);

        DecryptedLoginMeResponse decryptedLoginResponse = decryptLoginMeOrThrow(loginMeResponse);

        memberModifyService.upsertMember(decryptedLoginResponse);

        issueTokensToResponse(decryptedLoginResponse.userKey(), response);

        return true;
    }

    public Boolean reissueToken(ReissueRequest reissueRequest, HttpServletResponse response) {
        String presentedRt = reissueRequest.refreshToken();

        if (isInvalidRefreshToken(presentedRt)) {
            return false;
        }

        try {
            validateRefreshToken(presentedRt);
            Long userKey = jwtUtil.getUserKeyFromSubject(presentedRt);
            verifyStoredRefreshToken(userKey, presentedRt);

            issueNewTokens(userKey, response);

            return true;
        } catch (JwtException | IllegalArgumentException | BadCredentialsException e) {
            throw new CustomException(INVALID_REFRESH_TOKEN);
        }
    }

    public Boolean logout(HttpServletRequest request) {
        String accessToken = extractAndValidateAccessToken(request);
        Long userKey = jwtUtil.getUserKeyFromSubject(accessToken);

        blacklistAccessToken(accessToken);
        tokenStore.deleteRefresh(userKey);

        return true;
    }



    private LoginMeResponse.Success getTossUserInfo(TossLoginRequest tossLoginRequest) {
        try {
            SSLContext context = tossApiClient.createSSLContext(publicCrt, privateKey);
            String tossAccessToken = tossApiClient.getAccessToken(context, tossLoginRequest);
            return tossApiClient.getLoginMe(context, tossAccessToken);
        } catch (Exception e) {
            log.error("[TossAuthService Error] : Toss Access Token 발급 중 오류가 발생했습니다.", e);
            throw new CustomException(TOSS_API_CONNECTION_ERROR);
        }
    }

    private DecryptedLoginMeResponse decryptLoginMeOrThrow(LoginMeResponse.Success loginMeResponse) {
        try {
            return tossMemberInfoDecryptService.decryptResponse(loginMeResponse);
        } catch (Exception e) {
            log.error("[TossAuthService Error] 유저 정보 복호화 실패", e);
            throw new CustomException(TOSS_DECRYPT_ERROR);
        }
    }

    private void issueTokensToResponse(Long userKey, HttpServletResponse response) {
        String accessToken = jwtUtil.createJWT(accessTokenCategory, userKey, Role.ROLE_MEMBER.name());
        String refreshToken = jwtUtil.createJWT(refreshTokenCategory, userKey, Role.ROLE_MEMBER.name());

        tokenStore.saveRefreshToken(userKey, refreshToken, Duration.ofMillis(refreshTokenExpireMs));
        setTokenHeaders(response, accessToken, refreshToken);
    }

    private boolean isInvalidRefreshToken(String refreshToken) {
        return refreshToken == null || refreshToken.isBlank();
    }

    private void validateRefreshToken(String refreshToken) {
        jwtUtil.isExpired(refreshToken);
        jwtUtil.validateIssuer(refreshToken, tokenIss);

        // 토큰이 refresh 인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getClaimFromToken(refreshToken, "category", String.class);
        if (!refreshTokenCategory.equals(category)) {
            throw new BadCredentialsException(INVALID_REFRESH_TOKEN.getMessage());
        }
    }

    private void verifyStoredRefreshToken(Long userKey, String presentedToken) {
        // 서버 저장 해시와 대조 (재사용/탈취 감지)
        String savedRefresh = tokenStore.getRefreshToken(userKey)
                .orElseThrow(() -> new BadCredentialsException(INVALID_REFRESH_TOKEN.getMessage()));

        if (!presentedToken.equals(savedRefresh)) {
            tokenStore.deleteRefresh(userKey);
            throw new BadCredentialsException(INVALID_REFRESH_TOKEN.getMessage());
        }
    }

    private void issueNewTokens(Long userKey, HttpServletResponse response) {
        String newAccessToken = jwtUtil.createJWT(accessTokenCategory, userKey, Role.ROLE_MEMBER.name());
        String newRefreshToken = jwtUtil.createJWT(refreshTokenCategory, userKey, Role.ROLE_MEMBER.name());

        tokenStore.saveRefreshToken(userKey, newRefreshToken, Duration.ofMillis(refreshTokenExpireMs));
        setTokenHeaders(response, newAccessToken, newRefreshToken);
    }

    private void setTokenHeaders(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        response.setHeader("X-Refresh-Token", "Bearer " + refreshToken);
    }

    private String extractAndValidateAccessToken(HttpServletRequest request) {
        String accessToken = jwtUtil.resolveToken(request);
        jwtUtil.isExpired(accessToken);
        jwtUtil.validateIssuer(accessToken, tokenIss);
        return accessToken;
    }

    private void blacklistAccessToken(String accessToken) {
        long ttlSeconds = calculateTtlSeconds(accessToken);
        if (ttlSeconds > 0) {
            String key = jwtUtil.getJti(accessToken);
            blacklistService.blacklist(key, ttlSeconds);
        }
    }

    private long calculateTtlSeconds(String accessToken) {
        long expirationTime = jwtUtil.getExpiration(accessToken).getTime();
        long currentTime = System.currentTimeMillis();
        return Math.max(0, (expirationTime - currentTime) / 1000L);
    }
}
