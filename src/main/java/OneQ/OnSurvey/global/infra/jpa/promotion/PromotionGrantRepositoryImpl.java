package OneQ.OnSurvey.global.infra.jpa.promotion;

import OneQ.OnSurvey.global.promotion.entity.PromotionGrant;
import OneQ.OnSurvey.global.promotion.port.out.PromotionGrantRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static OneQ.OnSurvey.global.promotion.entity.QPromotionGrant.promotionGrant;

@Repository
@RequiredArgsConstructor
public class PromotionGrantRepositoryImpl implements PromotionGrantRepository {

    private final PromotionGrantJpaRepository promotionGrantJpaRepository;
    private final JPAQueryFactory queryFactory;

    @PersistenceContext
    private final EntityManager em;

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

    @Override
    public int markPointGrantedIfFalse(Long grantId) {
        long updated = queryFactory
                .update(promotionGrant)
                .set(promotionGrant.pointGranted, true)
                .where(
                        promotionGrant.id.eq(grantId),
                        promotionGrant.pointGranted.isFalse()
                )
                .execute();

        em.flush();
        em.clear();

        return (int) updated;
    }
}
