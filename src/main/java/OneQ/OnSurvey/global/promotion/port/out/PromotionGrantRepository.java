package OneQ.OnSurvey.global.promotion.port.out;

import OneQ.OnSurvey.domain.admin.api.dto.response.SurveyGrantStatsResponse;
import OneQ.OnSurvey.global.promotion.entity.PromotionGrant;

import java.util.List;
import java.util.Optional;

public interface PromotionGrantRepository {
    PromotionGrant save(PromotionGrant of);
    Optional<PromotionGrant> findByUserKeyAndSurveyIdAndPromotionCode(Long userKey, Long surveyId, String promotionCode);
    Optional<PromotionGrant> findById(Long grantId);
    PromotionGrant saveAndFlush(PromotionGrant g);
    int markPointGrantedIfFalse(Long grantId);

    List<PromotionGrant> findPendingWithExecKey(int limit);

    List<SurveyGrantStatsResponse> findSurveyGrantStats();
}
