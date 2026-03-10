package OneQ.OnSurvey.global.push.application.port.out;

import OneQ.OnSurvey.global.infra.toss.common.dto.push.PushResultResponse;
import OneQ.OnSurvey.global.infra.toss.common.dto.push.PushTemplateSendRequest;

import javax.net.ssl.SSLContext;
import java.io.IOException;

public interface TossPushPort {

    SSLContext createSSLContext(String certPath, String keyPath) throws Exception;

    PushResultResponse sendPush(SSLContext sslContext, PushTemplateSendRequest request) throws IOException;

}
