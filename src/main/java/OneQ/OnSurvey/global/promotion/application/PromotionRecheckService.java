package OneQ.OnSurvey.global.promotion.application;

import OneQ.OnSurvey.global.auth.token.TokenStore;
import OneQ.OnSurvey.global.infra.toss.common.dto.promotion.recheck.PromotionRecheckPendingResponse;
import OneQ.OnSurvey.global.promotion.entity.PromotionGrant;
import OneQ.OnSurvey.global.promotion.port.out.PromotionGrantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionRecheckService {

    private static final Duration LOCK_TTL = Duration.ofMinutes(5);

    private final PromotionGrantRepository promotionGrantRepository;
    private final PromotionUseCase promotionUseCase;
    private final TokenStore tokenStore;

    @Value("${toss.api.promotion.code}")
    private String promotionCode;

    public PromotionRecheckPendingResponse recheckPending(int limit) {
        List<PromotionGrant> pendings = promotionGrantRepository.findPendingWithExecKey(limit);

        int picked = pendings.size();
        int processed = 0;
        int errors = 0;

        for (PromotionGrant g : pendings) {
            String lockKey = buildLockKey(g.getUserKey(), g.getSurveyId());
            if (!tokenStore.acquireLock(lockKey, LOCK_TTL)) continue;

            try {
                promotionUseCase.recheckPendingGrant(g.getId());
                processed++;
            } catch (Exception e) {
                errors++;
            } finally {
                tokenStore.releaseLock(lockKey);
            }
        }

        return new PromotionRecheckPendingResponse(picked, processed, errors);
    }

    private String buildLockKey(long userKey, long surveyId) {
        return "promo:lock:" + promotionCode + ":user:" + userKey + ":survey:" + surveyId;
    }
}

