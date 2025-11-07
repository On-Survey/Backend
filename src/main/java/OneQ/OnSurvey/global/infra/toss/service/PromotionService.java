package OneQ.OnSurvey.global.infra.toss.service;

import OneQ.OnSurvey.global.auth.token.TokenStore;
import OneQ.OnSurvey.global.exception.CustomException;
import OneQ.OnSurvey.global.infra.toss.PromotionGrant;
import OneQ.OnSurvey.global.infra.toss.TossApiException;
import OneQ.OnSurvey.global.infra.toss.TossErrorCode;
import OneQ.OnSurvey.global.infra.toss.TossErrorMapper;
import OneQ.OnSurvey.global.infra.toss.adapter.TossApiClient;
import OneQ.OnSurvey.global.infra.toss.dto.ExecutePromotionResponse;
import OneQ.OnSurvey.global.infra.toss.dto.ExecutionResultResponse;
import OneQ.OnSurvey.global.infra.toss.dto.PromotionKeyResponse;
import OneQ.OnSurvey.global.infra.toss.repository.PromotionGrantRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.net.ssl.SSLContext;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionService {

    private static final Duration LOCK_TTL = Duration.ofMinutes(5);
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
    private final PromotionGrantRepository promotionGrantRepository;
    private final PromotionGrantTxService grantTx;

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
    @Transactional
    public ExecutionResultResponse issueAndConfirm(long userKey, long surveyId) {
        Long grantId = grantTx.getOrCreate(userKey, surveyId, promotionCode);
        PromotionGrant grant = promotionGrantRepository.findById(grantId)
                .orElseThrow(() -> new CustomException(TossErrorCode.TOSS_PROMOTION_NOT_FOUND));

        if (grant.isSuccess()) {
            return ExecutionResultResponse.success();
        }

        if (grant.isPending() && grant.getExecKey() != null) {
            String lockKey = buildLockKey(userKey, surveyId);
            if (!tokenStore.acquireLock(lockKey, LOCK_TTL)) {
                return ExecutionResultResponse.pending();
            }
            try {
                return pollWithRecoveryAndPersist(grant, userKey, grant.getExecKey());
            } finally {
                tokenStore.releaseLock(lockKey);
            }
        }

        // 최초 실행 / 재시도 실행 경로
        String lockKey = buildLockKey(userKey, surveyId);
        if (!tokenStore.acquireLock(lockKey, LOCK_TTL)) {
            return ExecutionResultResponse.pending();
        }

        try {
            String execKey = grant.getExecKey();
            if (execKey == null || isKeyExpired(grant)) {
                PromotionKeyResponse keyResp = tossApiClient.getPromotionKey(userKey, tossSslContext);
                execKey = keyResp.key();
                grantTx.markPending(grant.getId(), execKey);
            }

            ExecutePromotionResponse execResp = tossApiClient.executePromotionWithRetry(
                    userKey, promotionCode, execKey, promotionAmount, 2, tossSslContext);
            grantTx.saveExecKey(grant.getId(), execResp.key());

            ExecutionResultResponse finalRes = waitResultUntilFinalWithRecovery(
                    grant, userKey, promotionCode, execResp.key(), confirmWaitMs);

            switch (finalRes.status()) {
                case "SUCCESS" -> grantTx.markSuccess(grant.getId());
                case "PENDING" -> grantTx.markPending(grant.getId(), execResp.key());
                default        -> grantTx.markFail(grant.getId());
            }

            log.info("[PROMO] userKey={} surveyId={} code={} amount={} execKey={} status={}",
                    userKey, surveyId, maskKey(promotionCode), promotionAmount, maskKey(execResp.key()), finalRes.status());

            if ("FAILED".equals(finalRes.status()))
                throw new CustomException(TossErrorCode.TOSS_PROMOTION_API_ERROR);
            return finalRes;

        } catch (TossApiException te) {
            log.warn("[PROMO] tossCode={} msg={}", te.getCode(), te.getMessage());
            grantTx.markFail(grant.getId());
            throw new CustomException(TossErrorMapper.map(te.getCode()));
        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            log.error("[PROMO] err={}", e.toString());
            grantTx.markFail(grant.getId());
            throw new CustomException(TossErrorCode.TOSS_API_CONNECTION_ERROR);
        } finally {
            tokenStore.releaseLock(lockKey);
        }
    }

    private ExecutionResultResponse pollWithRecoveryAndPersist(PromotionGrant grant, long userKey, String execKey) {
        try {
            ExecutionResultResponse res = waitResultUntilFinalWithRecovery(
                    grant, userKey, promotionCode, execKey, confirmWaitMs);
            switch (res.status()) {
                case "SUCCESS" -> grantTx.markSuccess(grant.getId());
                case "PENDING" -> grantTx.markPending(grant.getId(), execKey);
                default        -> grantTx.markFail(grant.getId());
            }
            if ("FAILED".equals(res.status()))
                throw new CustomException(TossErrorCode.TOSS_PROMOTION_API_ERROR);
            return res;
        } catch (TossApiException te) {
            grantTx.markFail(grant.getId());
            throw new CustomException(TossErrorMapper.map(te.getCode()));
        } catch (Exception e) {
            grantTx.markFail(grant.getId());
            throw new CustomException(TossErrorCode.TOSS_API_CONNECTION_ERROR);
        }
    }

    /** execution-result를 성공/실패가 나오거나 타임아웃될 때까지 백오프 폴링 */
    private ExecutionResultResponse waitResultUntilFinalWithRecovery(
            PromotionGrant grant, long userKey, String promoCode, String execKey, long waitMs) throws Exception {

        if (waitMs <= 0) {
            return getResultOrRecoverOnce(grant, userKey, promoCode, execKey);
        }

        long deadline = System.currentTimeMillis() + waitMs;
        long[] sleeps = {200, 400, 800, 1600, 1600, 1600};
        int i = 0;

        while (true) {
            try {
                ExecutionResultResponse res = tossApiClient.getPromotionResult(userKey, promoCode, execKey, tossSslContext);
                if (res.isSuccess() || "FAILED".equals(res.status())) return res;
            } catch (TossApiException te) {
                if (te.getCode() == 4111) {
                    // 아직 execute가 반영 안 됐다고 판단 → 1회 보강
                    ExecutePromotionResponse execResp =
                            tossApiClient.executePromotionWithRetry(userKey, promoCode, execKey, promotionAmount, 1, tossSslContext);
                    execKey = execResp.key();
                    grantTx.saveExecKey(grant.getId(), execKey);
                    // 다음 루프에서 다시 조회
                } else {
                    return new ExecutionResultResponse("FAILED");
                }
            }

            if (System.currentTimeMillis() >= deadline) {
                return new ExecutionResultResponse("PENDING");
            }
            Thread.sleep(sleeps[Math.min(i++, sleeps.length - 1)]);
        }
    }

    private ExecutionResultResponse getResultOrRecoverOnce(
            PromotionGrant grant, long userKey, String promoCode, String execKey) throws Exception {
        try {
            return tossApiClient.getPromotionResult(userKey, promoCode, execKey, tossSslContext);
        } catch (TossApiException te) {
            if (te.getCode() == 4111) {
                ExecutePromotionResponse execResp =
                        tossApiClient.executePromotionWithRetry(userKey, promoCode, execKey, promotionAmount, 1, tossSslContext);
                grantTx.saveExecKey(grant.getId(), execResp.key());
                return tossApiClient.getPromotionResult(userKey, promoCode, execResp.key(), tossSslContext);
            }
            throw te;
        }
    }

    private boolean isKeyExpired(PromotionGrant g) {
        return g.getExecKeyIssuedAt() != null &&
                Duration.between(g.getExecKeyIssuedAt(), Instant.now()).compareTo(KEY_TTL) > 0;
    }

    private String buildLockKey(long userKey, long surveyId) {
        return "promo:lock:" + promotionCode + ":user:" + userKey + ":survey:" + surveyId;
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
