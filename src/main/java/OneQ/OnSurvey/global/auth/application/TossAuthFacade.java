package OneQ.OnSurvey.global.auth.application;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.service.MemberModifyService;
import OneQ.OnSurvey.domain.member.service.MemberQueryService;
import OneQ.OnSurvey.global.auth.dto.DecryptedLoginMeResponse;
import OneQ.OnSurvey.global.auth.port.out.TossAuthPort;
import OneQ.OnSurvey.global.common.exception.CustomException;
import OneQ.OnSurvey.global.common.util.JwtDecodeUtils;
import OneQ.OnSurvey.global.infra.discord.notifier.AlertNotifier;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.TossAccessTokenAlert;
import OneQ.OnSurvey.global.infra.toss.auth.TossMemberInfoDecryptService;
import OneQ.OnSurvey.global.infra.toss.auth.TossUnlinkValue;
import OneQ.OnSurvey.global.infra.toss.common.dto.auth.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.net.ssl.SSLContext;
import java.io.IOException;

import static OneQ.OnSurvey.global.auth.AuthErrorCode.INVALID_REFRESH_TOKEN;
import static OneQ.OnSurvey.global.common.exception.ErrorCode.UNAUTHORIZED;
import static OneQ.OnSurvey.global.infra.toss.common.exception.TossErrorCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TossAuthFacade implements AuthUseCase {

    @Value("${toss.secret.private-key}")
    private String privateKey;

    @Value("${toss.secret.public-crt}")
    private String publicCrt;

    @Value("${redis.global-key-prefix.daily-user}")
    private String dailyUserKeyPrefix;

    private final StringRedisTemplate redisTemplate;
    private final TossAuthPort tossAuthPort;
    private final MemberModifyService memberModifyService;
    private final TossMemberInfoDecryptService tossMemberInfoDecryptService;
    private final WithdrawalService withdrawalService;
    private final MemberQueryService memberQueryService;

    private final AlertNotifier alertNotifier;

    private SSLContext sslContext;

    @PostConstruct
    public void init() throws Exception {
        this.sslContext = tossAuthPort.createSSLContext(publicCrt, privateKey);
    }

    @Override
    public TossLoginResponse createAccessAndRefreshToken(TossLoginRequest tossLoginRequest, HttpServletResponse response) {
        try {
            TossTokenResponse token = tossAuthPort.getAccessToken(sslContext, tossLoginRequest);

            LoginMeResponse.Success me = tossAuthPort.getLoginMe(sslContext, token.accessToken());
            DecryptedLoginMeResponse decrypted = decryptLoginMeOrThrow(me);
            Member member = memberModifyService.upsertMember(decrypted);
            response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken());
            if (token.refreshToken() != null) {
                response.setHeader("X-Refresh-Token", "Bearer " + token.refreshToken());
            }

            updateDailyUser(member.getUserKey());
            return TossLoginResponse.of(member.isOnboardingCompleted());
        } catch (Exception e) {
            log.error("[TossAuthService-login] {}", e.getMessage(), e);
            throw new CustomException(TOSS_API_CONNECTION_ERROR);
        }
    }

    @Override
    public boolean reissueToken(TossReissueRequest request, HttpServletResponse response) {
        String presentedRt = request != null ? request.refreshToken() : null;
        if (presentedRt == null || presentedRt.isBlank()) {
            throw new CustomException(INVALID_REFRESH_TOKEN);
        }
        String rt = stripBearer(presentedRt);
        setToken(rt, response);

        return true;
    }

    @Override
    public String reissueTokenAndRetrieveAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String presentedRt = request.getHeader("X-Refresh-Token");
        if (presentedRt == null || presentedRt.isBlank()) {
            throw new CustomException(INVALID_REFRESH_TOKEN);
        }
        String rt = stripBearer(presentedRt);
        return setToken(rt, response);
    }

    @Override
    public boolean logoutByAccessToken(HttpServletRequest request) {
        try {
            String at = resolveBearer(request);
            if (at == null) return false;
            return tossAuthPort.removeByAccessToken(sslContext, at);
        } catch (Exception e) {
            log.error("[TossAuthService-logoutByAT] {}", e.getMessage(), e);
            throw new CustomException(TOSS_API_CONNECTION_ERROR);
        }
    }

    @Override
    public boolean logoutByUserKey(long userKey) {
        try {
            return tossAuthPort.removeByUserKey(sslContext, userKey);
        } catch (Exception e) {
            log.error("[TossAuthService-logoutByUserKey] {}", e.getMessage(), e);
            throw new CustomException(TOSS_API_CONNECTION_ERROR);
        }
    }

    @Override
    public LoginMeResponse.Success authenticateWithToss(HttpServletRequest request, HttpServletResponse response) {
        String at = resolveBearer(request);
        try {
            if (at == null || at.isBlank()) {
                log.warn("[TOSS:AUTH] Access Token이 비어있습니다. - RequestURI: {}:{}", request.getMethod(), request.getRequestURI());
                alertNotifier.sendTossAccessTokenAsync(
                    new TossAccessTokenAlert(at, "401", "EMPTY TOKEN")
                );
                throw new CustomException(UNAUTHORIZED);
            }

            if (JwtDecodeUtils.isTokenExpired(at)) {
                at = reissueTokenAndRetrieveAccessToken(request, response);
            }
            return tossAuthPort.getLoginMe(sslContext, at);
        } catch (IOException e) {
            throw new CustomException(UNAUTHORIZED);
        } catch (Exception e) {
            throw new CustomException(TOSS_API_CONNECTION_ERROR);
        }
    }

    @Override
    @Transactional
    public void unlink(Long userKey, TossUnlinkValue referrer) {
        Member member = memberQueryService.getMemberByUserKey(userKey);

        try {
            logoutByUserKey(userKey);
        } catch (Exception e) {
            log.warn("[TossUnlinkService] removeByUserKey failed: userKey={}, err={}", userKey, e.toString());
        }

        switch (referrer) {
            case UNLINK -> memberModifyService.changeMemberStatusTossConnectOut(member);
            case WITHDRAWAL_TOSS, WITHDRAWAL_TERMS -> withdrawalService.deleteAllInfo(userKey);
            default -> throw new CustomException(INVALID_REFERRER);
        }
    }

    private void updateDailyUser(Long userKey) {
        try {
            redisTemplate.opsForZSet().addIfAbsent(dailyUserKeyPrefix, String.valueOf(userKey), System.currentTimeMillis());
        } catch (Exception e) {
            log.warn("[TossAuthFacade] 일간 활성 사용자 업데이트 실패 - userKey: {}", userKey, e);
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

    private String setToken(String rt, HttpServletResponse res) {
        try {
            TossTokenResponse token = tossAuthPort.refreshOauth2Token(sslContext, rt);

            res.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken());
            if (token.refreshToken() != null) {
                res.setHeader("X-Refresh-Token", "Bearer " + token.refreshToken());
            }

            return token.accessToken();
        } catch (IOException e) {
            log.error("[TossAuthService-reissue] {}", e.getMessage(), e);
            throw new CustomException(INVALID_REFRESH_TOKEN);
        } catch (Exception e) {
            log.error("[TossAuthService-reissue] {}", e.getMessage(), e);
            throw new CustomException(TOSS_API_CONNECTION_ERROR);
        }
    }
}
