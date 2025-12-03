package OneQ.OnSurvey.global.payment.port.out;

import OneQ.OnSurvey.global.infra.toss.iap.dto.OrderStatusResponse;

import javax.net.ssl.SSLContext;
import java.io.IOException;

public interface TossIapPort {
    SSLContext createSSLContext(String certPath, String keyPath) throws Exception;

    OrderStatusResponse getIapOrderStatus(SSLContext ctx, long userKey, String orderId) throws IOException;
}
