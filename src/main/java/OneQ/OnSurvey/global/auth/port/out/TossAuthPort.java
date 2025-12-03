package OneQ.OnSurvey.global.auth.port.out;

import OneQ.OnSurvey.global.infra.toss.common.dto.auth.LoginMeResponse;
import OneQ.OnSurvey.global.infra.toss.common.dto.auth.TossLoginRequest;
import OneQ.OnSurvey.global.infra.toss.common.dto.auth.TossTokenResponse;

import javax.net.ssl.SSLContext;
import java.io.IOException;

public interface TossAuthPort {

    SSLContext createSSLContext(String certPath, String keyPath) throws Exception;

    /** OAuth 토큰 발급 */
    TossTokenResponse getAccessToken(SSLContext ctx, TossLoginRequest req) throws IOException;

    /** OAuth 토큰 재발급 */
    TossTokenResponse refreshOauth2Token(SSLContext ctx, String refreshToken) throws IOException;

    /** 토스 연결 끊기(AT 기준) */
    boolean removeByAccessToken(SSLContext ctx, String accessToken) throws IOException;

    /** 토스 연결 끊기(userKey 기준) */
    boolean removeByUserKey(SSLContext ctx, long userKey) throws IOException;

    /** 토스 me 정보 조회 */
    LoginMeResponse.Success getLoginMe(SSLContext ctx, String accessToken) throws IOException;
}
