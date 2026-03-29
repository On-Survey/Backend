package OneQ.OnSurvey.global.infra.jpa.promotion;

import OneQ.OnSurvey.global.promotion.PromotionGrantStatsProjection;
import OneQ.OnSurvey.global.promotion.entity.GrantStatus;
import OneQ.OnSurvey.global.promotion.entity.PromotionGrant;
import OneQ.OnSurvey.global.promotion.port.out.PromotionGrantRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Override
    public List<PromotionGrant> findPendingWithExecKey(int limit) {
        return queryFactory
                .selectFrom(promotionGrant)
                .where(
                        promotionGrant.status.eq(GrantStatus.PENDING),
                        promotionGrant.execKey.isNotNull()
                )
                .orderBy(promotionGrant.updatedAt.asc(), promotionGrant.id.asc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<PromotionGrantStatsProjection> findSurveyGrantStats() {
        return queryFactory
                .select(Projections.constructor(PromotionGrantStatsProjection.class,
                        promotionGrant.surveyId,
                        promotionGrant.count(),
                        Expressions.numberTemplate(Long.class,
                                "sum(case when {0} = {1} then 1 else 0 end)",
                                promotionGrant.status, Expressions.constant(GrantStatus.SUCCESS)),
                        Expressions.numberTemplate(Long.class,
                                "sum(case when {0} = {1} then 1 else 0 end)",
                                promotionGrant.status, Expressions.constant(GrantStatus.FAILED)),
                        Expressions.numberTemplate(Long.class,
                                "sum(case when {0} = {1} then 1 else 0 end)",
                                promotionGrant.status, Expressions.constant(GrantStatus.PENDING)),
                        promotionGrant.updatedAt.max()
                ))
                .from(promotionGrant)
                .groupBy(promotionGrant.surveyId)
                .orderBy(promotionGrant.updatedAt.max().desc())
                .fetch();
    }
}
