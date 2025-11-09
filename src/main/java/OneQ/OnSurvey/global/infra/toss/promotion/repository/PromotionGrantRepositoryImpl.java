package OneQ.OnSurvey.global.infra.toss.promotion.repository;

import OneQ.OnSurvey.global.infra.toss.promotion.PromotionGrant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PromotionGrantRepositoryImpl implements PromotionGrantRepository {

    private final PromotionGrantJpaRepository promotionGrantJpaRepository;

    @Override
    public PromotionGrant save(PromotionGrant promotionGrant) {
        return promotionGrantJpaRepository.save(promotionGrant);
    }

    @Override
    public Optional<PromotionGrant> findByUserKeyAndSurveyIdAndPromotionCode(Long userKey, Long surveyId, String promotionCode) {
        return promotionGrantJpaRepository.findByUserKeyAndSurveyIdAndPromotionCode(userKey, surveyId, promotionCode);
    }

    @Override
    public Optional<PromotionGrant> findById(Long grantId) {
        return promotionGrantJpaRepository.findById(grantId);
    }

    @Override
    public PromotionGrant saveAndFlush(PromotionGrant g) {
        promotionGrantJpaRepository.saveAndFlush(g);
        return g;
    }
}
