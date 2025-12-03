package OneQ.OnSurvey.global.infra.jpa.promotion;

import OneQ.OnSurvey.global.promotion.entity.PromotionGrant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PromotionGrantJpaRepository extends JpaRepository<PromotionGrant, Long> {
    Optional<PromotionGrant> findByUserKeyAndSurveyIdAndPromotionCode(Long userKey, Long surveyId, String promotionCode);
}
