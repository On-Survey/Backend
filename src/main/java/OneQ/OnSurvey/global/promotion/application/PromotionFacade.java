package OneQ.OnSurvey.global.promotion.application;

import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.MemberErrorCode;
import OneQ.OnSurvey.domain.member.repository.MemberRepository;
import OneQ.OnSurvey.domain.survey.SurveyErrorCode;
import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.service.SurveyGlobalStatsService;
import OneQ.OnSurvey.domain.survey.service.query.SurveyQueryService;
import OneQ.OnSurvey.global.auth.token.TokenStore;
import OneQ.OnSurvey.global.common.exception.CustomException;
import OneQ.OnSurvey.global.infra.toss.common.dto.promotion.ExecutePromotionResponse;
import OneQ.OnSurvey.global.infra.toss.common.dto.promotion.ExecutionResultResponse;
import OneQ.OnSurvey.global.infra.toss.common.dto.promotion.PromotionKeyResponse;
import OneQ.OnSurvey.global.infra.toss.common.exception.TossApiException;
import OneQ.OnSurvey.global.infra.toss.common.exception.TossErrorCode;
import OneQ.OnSurvey.global.infra.toss.common.exception.TossErrorMapper;
import OneQ.OnSurvey.global.promotion.entity.PromotionGrant;
import OneQ.OnSurvey.global.promotion.port.out.PromotionGrantRepository;
import OneQ.OnSurvey.global.promotion.port.out.TossPromotionPort;
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
public class PromotionFacade implements PromotionUseCase {

    private static final Duration LOCK_TTL = Duration.ofMinutes(5);
    private static final Duration KEY_TTL = Duration.ofHours(1);

    @Value("${toss.secret.private-key}")
    private String privateKey;

    @Value("${toss.secret.public-crt}")
    private String publicCrt;

    @Value("${toss.api.promotion.confirm-wait-ms}")
    private long confirmWaitMs;

    private final TossPromotionPort tossPromotionPort;
    private final TokenStore tokenStore;
    private final PromotionGrantRepository promotionGrantRepository;
    private final PromotionGrantTxService grantTx;
    private final MemberRepository memberRepository;
    private final SurveyGlobalStatsService surveyGlobalStatsService;
    private final SurveyQueryService surveyQueryService;
    private final PromotionTierResolver promotionTierResolver;

    private SSLContext tossSslContext;

    @PostConstruct
    void initSsl() throws Exception {
        this.tossSslContext = tossPromotionPort.createSSLContext(publicCrt, privateKey);
    }

    @Override
    @Transactional
    public ExecutionResultResponse issueAndConfirm(long userKey, long surveyId) {
        log.info("[PROMO] 프로모션 지급 시도 userKey={} surveyId={}", userKey, surveyId);

        Survey survey = surveyQueryService.getSurveyById(surveyId);

        if (Boolean.TRUE.equals(survey.getIsFree())) {
            log.info("[PROMO] 무료 설문 지급 차단 userKey={} surveyId={}", userKey, surveyId);
            throw new CustomException(SurveyErrorCode.SURVEY_FREE_PROMOTION_NOT_ALLOWED);
        }

        PromoTier tier = promotionTierResolver.resolveBysurveyId(surveyId);

        // 최초 실행 / 재시도 실행 경로
        Long grantId = upsertGrantId(userKey, surveyId, tier.code());

        String lockKey = buildLockKey(userKey, surveyId, tier.code());
        if (!tokenStore.acquireLock(lockKey, LOCK_TTL)) {
            return ExecutionResultResponse.pending();
        }

        try {
            PromotionGrant grant = promotionGrantRepository.findById(grantId)
                    .orElseThrow(() -> new CustomException(TossErrorCode.TOSS_PROMOTION_NOT_FOUND));

            if (grant.isSuccess()) {
                grantPromotionPointIfNeeded(grantId, userKey, tier.amount());
                return ExecutionResultResponse.success();
            }

            if (grant.isPending() && grant.getExecKey() != null) {
                return pollWithRecoveryAndPersist(grant, userKey, grant.getExecKey(), tier.code(), tier.amount());
            }

            try {
                String execKey = grant.getExecKey();
                if (execKey == null || isKeyExpired(grant)) {
                    PromotionKeyResponse keyResp = tossPromotionPort.getPromotionKey(userKey, tossSslContext);
                    execKey = keyResp.key();
                    grantTx.markPending(grant.getId(), execKey);
                }

                ExecutePromotionResponse execResp = tossPromotionPort.executePromotionWithRetry(
                        userKey, tier.code(), execKey, tier.amount(), 2, tossSslContext);
                grantTx.saveExecKey(grant.getId(), execResp.key());

                ExecutionResultResponse finalRes = waitResultUntilFinalWithRecovery(
                        grant, userKey, tier.code(), execResp.key(), tier.amount(), confirmWaitMs);

                switch (finalRes.status()) {
                    case "SUCCESS" -> {
                        grantTx.markSuccess(grant.getId());
                        grantPromotionPointIfNeeded(grantId, userKey, tier.amount());
                    }
                    case "PENDING" -> grantTx.markPending(grant.getId(), execResp.key());
                    default        -> grantTx.markFail(grant.getId());
                }

                log.info("[PROMO] userKey={} surveyId={} code={} amount={} execKey={} status={}",
                        userKey, surveyId, maskKey(tier.code()), tier.amount(), maskKey(execResp.key()), finalRes.status());

                if ("FAILED".equals(finalRes.status())) {
                    throw new CustomException(TossErrorCode.TOSS_PROMOTION_API_ERROR);
                }
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
            }

        } finally {
            tokenStore.releaseLock(lockKey);
        }
    }

    private Long upsertGrantId(long userKey, long surveyId, String promotionCode) {
        try {
            return grantTx.createOnly(userKey, surveyId, promotionCode);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            Long id = grantTx.findId(userKey, surveyId, promotionCode);
            if (id == null) throw e;
            return id;
        }
    }

    protected void grantPromotionPointIfNeeded(Long grantId, long userKey, int amount) {
        int updated = promotionGrantRepository.markPointGrantedIfFalse(grantId);
        if (updated == 0) return;

        Member member = memberRepository.findMemberByUserKey(userKey)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

        member.increasePromotionPoint(amount);
        surveyGlobalStatsService.addPromotionCount(1);
    }

    private ExecutionResultResponse pollWithRecoveryAndPersist(
            PromotionGrant grant, long userKey, String execKey, String promoCode, int amount) {
        try {
            ExecutionResultResponse res = waitResultUntilFinalWithRecovery(
                    grant, userKey, promoCode, execKey, amount, confirmWaitMs);
            switch (res.status()) {
                case "SUCCESS" -> {
                    grantTx.markSuccess(grant.getId());
                    grantPromotionPointIfNeeded(grant.getId(), userKey, amount);
                }
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
            PromotionGrant grant, long userKey, String promoCode, String execKey, int amount, long waitMs) throws Exception {

        if (waitMs <= 0) {
            return getResultOrRecoverOnce(grant, userKey, promoCode, execKey, amount);
        }

        long deadline = System.currentTimeMillis() + waitMs;
        long[] sleeps = {200, 400, 800, 1600, 1600, 1600};
        int i = 0;

        while (true) {
            try {
                ExecutionResultResponse res = tossPromotionPort.getPromotionResult(userKey, promoCode, execKey, tossSslContext);
                if (res.isSuccess() || "FAILED".equals(res.status())) return res;
            } catch (TossApiException te) {
                if (te.getCode() == 4111) {
                    // 아직 execute가 반영 안 됐다고 판단 → 1회 보강
                    ExecutePromotionResponse execResp =
                            tossPromotionPort.executePromotionWithRetry(userKey, promoCode, execKey, amount, 1, tossSslContext);
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
            PromotionGrant grant, long userKey, String promoCode, String execKey, int amount) throws Exception {
        try {
            return tossPromotionPort.getPromotionResult(userKey, promoCode, execKey, tossSslContext);
        } catch (TossApiException te) {
            if (te.getCode() == 4111) {
                ExecutePromotionResponse execResp =
                        tossPromotionPort.executePromotionWithRetry(userKey, promoCode, execKey, amount, 1, tossSslContext);
                grantTx.saveExecKey(grant.getId(), execResp.key());
                return tossPromotionPort.getPromotionResult(userKey, promoCode, execResp.key(), tossSslContext);
            }
            throw te;
        }
    }

    private boolean isKeyExpired(PromotionGrant g) {
        return g.getExecKeyIssuedAt() != null &&
                Duration.between(g.getExecKeyIssuedAt(), Instant.now()).compareTo(KEY_TTL) > 0;
    }

    private String buildLockKey(long userKey, long surveyId, String promoCode) {
        return "promo:lock:" + promoCode + ":user:" + userKey + ":survey:" + surveyId;
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

    @Transactional
    public void recheckPendingGrant(long grantId) {

        PromotionGrant grant = promotionGrantRepository.findById(grantId)
                .orElseThrow(() -> new CustomException(TossErrorCode.TOSS_PROMOTION_NOT_FOUND));

        // 이미 성공이면 포인트만 보정하고 종료
        if (grant.isSuccess()) {
            PromoTier tier = promotionTierResolver.resolveByCode(grant.getPromotionCode());
            grantPromotionPointIfNeeded(grantId, grant.getUserKey(), tier.amount());
            return;
        }

        if (!grant.isPending() || grant.getExecKey() == null) {
            return;
        }

        PromoTier tier = promotionTierResolver.resolveByCode(grant.getPromotionCode());
        ExecutionResultResponse res =
                pollWithRecoveryAndPersist(grant, grant.getUserKey(), grant.getExecKey(), tier.code(), tier.amount());

        switch (res.status()) {
            case "SUCCESS" -> {
                grantTx.markSuccess(grantId);
                grantPromotionPointIfNeeded(grantId, grant.getUserKey(), tier.amount());
            }
            case "PENDING" -> grantTx.markPending(grantId, grant.getExecKey());
            default        -> grantTx.markFail(grantId);
        }
    }
}
