package OneQ.OnSurvey.global.infra.toss.repository;

import OneQ.OnSurvey.global.infra.toss.PromotionGrant;

import java.util.Optional;

public interface PromotionGrantRepository {
    PromotionGrant save(PromotionGrant of);
    Optional<PromotionGrant> findByUserKeyAndSurveyIdAndPromotionCode(Long userKey, Long surveyId, String promotionCode);
    Optional<PromotionGrant> findById(Long grantId);
    void saveAndFlush(PromotionGrant g);
}
