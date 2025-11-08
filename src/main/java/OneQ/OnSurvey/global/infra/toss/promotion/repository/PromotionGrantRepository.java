package OneQ.OnSurvey.global.infra.toss.promotion.repository;

import OneQ.OnSurvey.global.infra.toss.promotion.PromotionGrant;

import java.util.Optional;

public interface PromotionGrantRepository {
    PromotionGrant save(PromotionGrant of);
    Optional<PromotionGrant> findByUserKeyAndSurveyIdAndPromotionCode(Long userKey, Long surveyId, String promotionCode);
    Optional<PromotionGrant> findById(Long grantId);
    PromotionGrant saveAndFlush(PromotionGrant g);
}
