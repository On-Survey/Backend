package OneQ.OnSurvey.global.push.adapter.in;

import OneQ.OnSurvey.global.push.application.port.in.PushUseCase;
import OneQ.OnSurvey.global.push.application.port.out.PushPropertyRepository;
import OneQ.OnSurvey.global.push.application.port.out.TossPushPort;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PushFacade implements PushUseCase {

    private final TossPushPort tossPushPort;
    private final PushPropertyRepository pushPropertyRepository;

    @Value("${toss.secret.private-key}")
    private String privateKey;

    @Value("${toss.secret.public-crt}")
    private String publicCrt;

    private SSLContext sslContext;

    @PostConstruct
    public void init() throws Exception {
        this.sslContext = tossPushPort.createSSLContext(publicCrt, privateKey);
    }

    @Override
    public boolean fillTemplateAndSendPush(long userKey, String templateCode, Map<String, String> templateCtx) {

        Map<String, String> pushProperties = pushPropertyRepository.findPushTemplateContextByCode(templateCode);
        if (pushProperties != null && !pushProperties.isEmpty()) {
            // DB에 저장된 기본값 이외의 templateCtx가 있으면 갱신
            pushProperties.keySet().forEach(
                key -> pushProperties.put(key, templateCtx.getOrDefault(key, pushProperties.get(key)))
            );
        }

        try {
            tossPushPort.sendPush(sslContext, userKey, templateCode, pushProperties);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
