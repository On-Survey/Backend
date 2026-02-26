package OneQ.OnSurvey.global.push.application.port.out;

import OneQ.OnSurvey.global.infra.toss.common.dto.push.PushResultResponse;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.Map;

public interface TossPushPort {

    SSLContext createSSLContext(String certPath, String keyPath) throws Exception;

    PushResultResponse sendPush(SSLContext sslContext, long userKey, String templateId, Map<String, String> templateCtx) throws IOException;

}
