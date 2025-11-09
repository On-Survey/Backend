package OneQ.OnSurvey.global.infra.toss.auth.service;

import OneQ.OnSurvey.domain.member.service.MemberModifyService;
import OneQ.OnSurvey.global.exception.CustomException;
import OneQ.OnSurvey.global.infra.toss.adapter.TossApiClient;
import OneQ.OnSurvey.global.infra.toss.auth.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import java.io.IOException;

import static OneQ.OnSurvey.global.auth.AuthErrorCode.INVALID_REFRESH_TOKEN;
import static OneQ.OnSurvey.global.exception.ErrorCode.UNAUTHORIZED;
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

    private final TossApiClient tossApiClient;
    private final MemberModifyService memberModifyService;
    private final TossMemberInfoDecryptService tossMemberInfoDecryptService;

    public boolean createAccessAndRefreshToken(TossLoginRequest tossLoginRequest, HttpServletResponse response) {
        try {
            SSLContext ctx = tossApiClient.createSSLContext(publicCrt, privateKey);
            String accessToken = tossApiClient.getAccessToken(ctx, tossLoginRequest);

            LoginMeResponse.Success me = tossApiClient.getLoginMe(ctx, accessToken);
            DecryptedLoginMeResponse decrypted = decryptLoginMeOrThrow(me);
            memberModifyService.upsertMember(decrypted);
            response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

            return true;
        } catch (Exception e) {
            log.error("[TossAuthService-login] {}", e.getMessage(), e);
            throw new CustomException(TOSS_API_CONNECTION_ERROR);
        }
    }

    public boolean reissueToken(TossReissueRequest request, HttpServletResponse response) {
        String presentedRt = request != null ? request.refreshToken() : null;
        if (presentedRt == null || presentedRt.isBlank()) {
            throw new CustomException(INVALID_REFRESH_TOKEN);
        }
        String rt = stripBearer(presentedRt);
        try {
            SSLContext ctx = tossApiClient.createSSLContext(publicCrt, privateKey);
            TossTokenResponse token = tossApiClient.refreshOauth2Token(ctx, rt);

            response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken());
            if (token.refreshToken() != null) {
                response.setHeader("X-Refresh-Token", "Bearer " + token.refreshToken());
            }
            return true;
        } catch (IOException e) {
            log.error("[TossAuthService-reissue] {}", e.getMessage(), e);
            throw new CustomException(INVALID_REFRESH_TOKEN);
        } catch (Exception e) {
            log.error("[TossAuthService-reissue] {}", e.getMessage(), e);
            throw new CustomException(TOSS_API_CONNECTION_ERROR);
        }
    }

    public boolean logoutByAccessToken(HttpServletRequest request) {
        try {
            String at = resolveBearer(request);
            if (at == null) return false;
            SSLContext ctx = tossApiClient.createSSLContext(publicCrt, privateKey);
            return tossApiClient.removeByAccessToken(ctx, at);
        } catch (Exception e) {
            log.error("[TossAuthService-logoutByAT] {}", e.getMessage(), e);
            throw new CustomException(TOSS_API_CONNECTION_ERROR);
        }
    }

    public boolean logoutByUserKey(long userKey) {
        try {
            SSLContext ctx = tossApiClient.createSSLContext(publicCrt, privateKey);
            return tossApiClient.removeByUserKey(ctx, userKey);
        } catch (Exception e) {
            log.error("[TossAuthService-logoutByUserKey] {}", e.getMessage(), e);
            throw new CustomException(TOSS_API_CONNECTION_ERROR);
        }
    }

    public LoginMeResponse.Success authenticateWithToss(HttpServletRequest request) {
        try {
            String at = resolveBearer(request);
            if (at == null || at.isBlank()) throw new CustomException(UNAUTHORIZED);
            SSLContext ctx = tossApiClient.createSSLContext(publicCrt, privateKey);
            return tossApiClient.getLoginMe(ctx, at);
        } catch (IOException e) {
            throw new CustomException(UNAUTHORIZED);
        } catch (Exception e) {
            throw new CustomException(TOSS_API_CONNECTION_ERROR);
        }
    }

    private String resolveBearer(HttpServletRequest request) {
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) return null;
        return auth.substring("Bearer ".length());
    }

    private String stripBearer(String token) {
        return token != null && token.startsWith("Bearer ")
                ? token.substring("Bearer ".length())
                : token;
    }

    private DecryptedLoginMeResponse decryptLoginMeOrThrow(LoginMeResponse.Success me) {
        try {
            return tossMemberInfoDecryptService.decryptResponse(me);
        } catch (Exception e) {
            log.error("[TossAuthService] 유저 정보 복호화 실패", e);
            throw new CustomException(TOSS_DECRYPT_ERROR);
        }
    }
}
