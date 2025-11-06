package OneQ.OnSurvey.global.infra.toss.service;

import OneQ.OnSurvey.global.auth.token.TokenStore;
import OneQ.OnSurvey.global.exception.CustomException;
import OneQ.OnSurvey.global.infra.toss.TossErrorCode;
import OneQ.OnSurvey.global.infra.toss.adapter.TossApiClient;
import OneQ.OnSurvey.global.infra.toss.dto.ExecutePromotionResponse;
import OneQ.OnSurvey.global.infra.toss.dto.ExecutionResultResponse;
import OneQ.OnSurvey.global.infra.toss.dto.PromotionKeyResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionService {

    private static final Duration KEY_TTL = Duration.ofHours(1);

    @Value("${toss.secret.private-key}")
    private String privateKey;

    @Value("${toss.secret.public-crt}")
    private String publicCrt;

    @Value("${toss.api.promotion.amount}")
    private int promotionAmount;

    @Value("${toss.api.promotion.code}")
    private String promotionCode;

    @Value("${toss.api.promotion.confirm-wait-ms}")
    private long confirmWaitMs;

    private final TossApiClient tossApiClient;
    private final TokenStore tokenStore;

    private SSLContext tossSslContext;

    @PostConstruct
    void initSsl() throws Exception {
        this.tossSslContext = tossApiClient.createSSLContext(publicCrt, privateKey);
    }

    /**
     * 토스 포인트 지급 실행 / 결과 검증
     * - SUCCESS : 그대로 반환
     * - FAILED  : CustomException throw
     * - PENDING : confirmWaitMs 타임아웃 내 최종 상태가 안 나오면 그대로 PENDING 반환
     */
    public ExecutionResultResponse issueAndConfirm(long userKey) {
        String baseKey = buildIdKey(promotionCode, userKey);
        long started = System.currentTimeMillis();

        try {
            PromotionKeyResponse keyResp = tossApiClient.getPromotionKey(userKey, tossSslContext);

            ExecutePromotionResponse execResp =
                    tossApiClient.executePromotionWithRetry(userKey, promotionCode, keyResp.key(), promotionAmount, 2, tossSslContext);

            String txId = execResp.key();
            tokenStore.saveValue(baseKey + ":lastKey", txId, KEY_TTL);

            ExecutionResultResponse finalRes =
                    waitResultUntilFinal(userKey, promotionCode, txId, confirmWaitMs);

            log.info("[PROMO] userKey={} promotionCode={} amount={} txId={} status={} elapsedMs={}",
                    userKey, maskKey(promotionCode), promotionAmount, txId, finalRes.status(),
                    System.currentTimeMillis() - started);

            return finalRes;

        } catch (CustomException ce) {
            log.warn("[PROMO] userKey={} promotionCode={} amount={} msg={}",
                    userKey, maskKey(promotionCode), promotionAmount, ce.getMessage());
            throw ce;

        } catch (Exception e) {
            log.error("[PROMO] userKey={} promotionCode={} amount={} err={}",
                    userKey, maskKey(promotionCode), promotionAmount, e.toString());
            throw new CustomException(TossErrorCode.TOSS_PROMOTION_API_ERROR);
        }
    }

    /** execution-result를 성공/실패가 나오거나 타임아웃될 때까지 백오프 폴링 */
    private ExecutionResultResponse waitResultUntilFinal(long userKey, String promoCode, String execKey, long waitMs)
            throws Exception {

        if (waitMs <= 0) {
            ExecutionResultResponse res = tossApiClient.getPromotionResult(userKey, promoCode, execKey, tossSslContext);
            if ("FAILED".equals(res.status())) {
                log.warn("[PROMO] poll failed immediately execKey={}", maskKey(execKey));
                throw new CustomException(TossErrorCode.TOSS_PROMOTION_API_ERROR);
            }
            return res;
        }

        long deadline = System.currentTimeMillis() + waitMs;
        long[] sleeps = {200, 400, 800, 1600, 1600, 1600}; // 최대 6.8초 백오프
        int i = 0, attempt = 1;

        while (true) {
            ExecutionResultResponse res = tossApiClient.getPromotionResult(userKey, promoCode, execKey, tossSslContext);

            if (res.isSuccess()) return res;
            if ("FAILED".equals(res.status())) {
                log.warn("[PROMO] poll failed attempt={} execKey={}", attempt, maskKey(execKey));
                throw new CustomException(TossErrorCode.TOSS_PROMOTION_API_ERROR);
            }

            if (System.currentTimeMillis() >= deadline) {
                log.warn("[PROMO] poll timeout attempt={} lastStatus=PENDING execKey={}", attempt, maskKey(execKey));
                return res;
            }

            Thread.sleep(sleeps[Math.min(i++, sleeps.length - 1)]);
            attempt++;
        }
    }

    /** 키: promo:{promotionCode}:{userKey} */
    private String buildIdKey(String promotionCode, long userKey) {
        return "promo:" + promotionCode + ":" + userKey;
    }

    /** 민감 정보 마스킹 */
    private static String maskKey(String key) {
        if (key == null || key.length() <= 4) {
            return "****";
        }

        int prefixLen = Math.min(3, key.length() / 4);
        int suffixLen = Math.min(3, key.length() / 4);

        String prefix = key.substring(0, prefixLen);
        String suffix = key.substring(key.length() - suffixLen);

        return prefix + "****" + suffix;
    }
}
