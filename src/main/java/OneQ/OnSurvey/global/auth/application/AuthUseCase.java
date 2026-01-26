package OneQ.OnSurvey.global.auth.application;

import OneQ.OnSurvey.global.infra.toss.auth.TossUnlinkValue;
import OneQ.OnSurvey.global.infra.toss.common.dto.auth.LoginMeResponse;
import OneQ.OnSurvey.global.infra.toss.common.dto.auth.TossLoginRequest;
import OneQ.OnSurvey.global.infra.toss.common.dto.auth.TossLoginResponse;
import OneQ.OnSurvey.global.infra.toss.common.dto.auth.TossReissueRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthUseCase {
    TossLoginResponse createAccessAndRefreshToken(TossLoginRequest tossLoginRequest, HttpServletResponse response);

    boolean reissueToken(TossReissueRequest request, HttpServletResponse response);

    boolean reissueToken(HttpServletRequest request, HttpServletResponse response);

    boolean logoutByAccessToken(HttpServletRequest request);

    boolean logoutByUserKey(long userKey);

    LoginMeResponse.Success authenticateWithToss(HttpServletRequest request, HttpServletResponse response);

    /**
     * 토스 연결 해지
     *  - UNLINK: 토스 연결만 해지, 멤버는 유지
     *  - WITHDRAWAL_TOSS / WITHDRAWAL_TERMS: 토스 연결 해지 후 서비스 탈퇴
     */
    void unlink(Long userKey, TossUnlinkValue referrer);
}
