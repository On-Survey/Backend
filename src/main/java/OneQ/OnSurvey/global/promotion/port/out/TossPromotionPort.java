package OneQ.OnSurvey.global.promotion.port.out;

import OneQ.OnSurvey.global.infra.toss.common.dto.promotion.ExecutePromotionResponse;
import OneQ.OnSurvey.global.infra.toss.common.dto.promotion.ExecutionResultResponse;
import OneQ.OnSurvey.global.infra.toss.common.dto.promotion.PromotionKeyResponse;

import javax.net.ssl.SSLContext;
import java.io.IOException;

public interface TossPromotionPort {

    SSLContext createSSLContext(String certPath, String keyPath) throws Exception;

    PromotionKeyResponse getPromotionKey(long userKey, SSLContext ctx) throws IOException;

    ExecutePromotionResponse executePromotionWithRetry(
            long userKey, String promotionCode, String key, int amount, int retries, SSLContext ctx
    ) throws Exception;

    ExecutionResultResponse getPromotionResult(
            long userKey, String promotionCode, String key, SSLContext ctx
    ) throws IOException;
}
