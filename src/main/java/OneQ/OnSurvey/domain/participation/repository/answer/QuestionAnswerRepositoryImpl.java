package OneQ.OnSurvey.domain.participation.repository.answer;

import OneQ.OnSurvey.domain.participation.entity.QuestionAnswer;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerStats;
import OneQ.OnSurvey.domain.survey.model.AgeRange;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.Residence;
import OneQ.OnSurvey.domain.survey.model.SurveyResponseFilterCondition;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

import static OneQ.OnSurvey.domain.member.QMember.member;
import static OneQ.OnSurvey.domain.participation.entity.QQuestionAnswer.questionAnswer;

@Repository
public class QuestionAnswerRepositoryImpl extends AbstractAnswerRepository<QuestionAnswer> {
    private final JPAQueryFactory jpaQueryFactory;

    public QuestionAnswerRepositoryImpl(
        AnswerJpaRepository<QuestionAnswer> answerJpaRepository,
        JPAQueryFactory jpaQueryFactory
    ) {
        super(answerJpaRepository);
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public List<QuestionAnswer> getAnswersByQuestionIdListAndMemberId(List<Long> questionIdList, Long memberId) {
        return jpaQueryFactory.selectFrom(questionAnswer)
            .where(
                questionAnswer.questionId.in(questionIdList),
                questionAnswer.memberId.eq(memberId)
            )
            .fetch();
    }

    @Override
    public List<AnswerStats> getAggregatedAnswersByQuestionIds(List<Long> questionIdList) {
        return jpaQueryFactory.select(Projections.constructor(AnswerStats.class,
                questionAnswer.questionId,
                questionAnswer.content,
                questionAnswer.answerId.count()
            ))
            .from(questionAnswer)
            .where(questionAnswer.questionId.in(questionIdList))
            .groupBy(questionAnswer.questionId, questionAnswer.content)
            .orderBy(questionAnswer.questionId.asc())
            .fetch();
    }

    @Override
    public List<AnswerStats> getAnswersByQuestionIds(List<Long> questionIdList) {
        return jpaQueryFactory.select(Projections.constructor(AnswerStats.class,
                questionAnswer.questionId,
                questionAnswer.content
            ))
            .from(questionAnswer)
            .where(questionAnswer.questionId.in(questionIdList))
            .orderBy(questionAnswer.questionId.asc())
            .fetch();
    }

    @Override
    public List<AnswerStats> getAggregatedAnswersByQuestionIds(
            List<Long> questionIds,
            SurveyResponseFilterCondition filter
    ) {
        SurveyResponseFilterCondition effective =
                (filter == null ? SurveyResponseFilterCondition.empty() : filter);

        return jpaQueryFactory
                .select(Projections.constructor(
                        AnswerStats.class,
                        questionAnswer.questionId,
                        questionAnswer.content,
                        questionAnswer.answerId.count()
                ))
                .from(questionAnswer)
                .join(member).on(member.id.eq(questionAnswer.memberId))
                .where(
                        questionAnswer.questionId.in(questionIds),
                        buildAgeCondition(member.birthDay, effective.ages()),
                        buildGenderCondition(member.gender, effective.genders()),
                        buildResidenceCondition(member.residence, effective.residences())
                )
                .groupBy(questionAnswer.questionId, questionAnswer.content)
                .orderBy(questionAnswer.questionId.asc())
                .fetch();
    }

    @Override
    public List<AnswerStats> getAnswersByQuestionIds(
            List<Long> questionIds,
            SurveyResponseFilterCondition filter
    ) {
        if (questionIds == null || questionIds.isEmpty()) {
            return List.of();
        }

        SurveyResponseFilterCondition effective =
                (filter == null) ? new SurveyResponseFilterCondition(null, null, null)
                        : filter;

        return jpaQueryFactory
                .select(Projections.constructor(
                        AnswerStats.class,
                        questionAnswer.questionId,
                        questionAnswer.content
                ))
                .from(questionAnswer)
                .join(member).on(questionAnswer.memberId.eq(member.id))
                .where(
                        questionAnswer.questionId.in(questionIds),
                        buildAgeCondition(member.birthDay, effective.ages()),
                        buildGenderCondition(member.gender, effective.genders()),
                        buildResidenceCondition(member.residence, effective.residences())
                )
                .fetch();
    }


    private BooleanExpression buildGenderCondition(EnumPath<Gender> genderPath, List<Gender> genders) {
        if (genders == null || genders.isEmpty()) return null;
        if (genders.contains(Gender.ALL)) return null;
        return genderPath.in(genders);
    }

    private BooleanExpression buildResidenceCondition(EnumPath<Residence> residencePath, List<Residence> residences) {
        if (residences == null || residences.isEmpty()) return null;
        if (residences.contains(Residence.ALL)) return null;
        return residencePath.in(residences);
    }

    private BooleanExpression buildAgeCondition(StringPath birthDayPath, List<AgeRange> ages) {
        if (ages == null || ages.isEmpty()) return null;
        if (ages.contains(AgeRange.ALL)) return null;

        BooleanExpression exp = null;
        for (AgeRange range : ages) {
            BooleanExpression one = ageRangeExpr(birthDayPath, range);
            if (one == null) continue;
            exp = (exp == null) ? one : exp.or(one);
        }
        return exp;
    }


    private BooleanExpression ageRangeExpr(StringPath birthDayPath, AgeRange range) {
        int minAge;
        int maxAge;

        switch (range) {
            case TEN     -> { minAge = 10; maxAge = 19; }
            case TWENTY  -> { minAge = 20; maxAge = 29; }
            case THIRTY  -> { minAge = 30; maxAge = 39; }
            case FOURTY  -> { minAge = 40; maxAge = 49; }
            case FIFTY   -> { minAge = 50; maxAge = 59; }
            case SIXTY   -> { minAge = 60; maxAge = 69; }
            case OVER    -> { minAge = 70; maxAge = 200; }
            default      -> { return null; }
        }

        return Expressions.booleanTemplate(
                "(YEAR(CURDATE()) - CAST(SUBSTRING({0}, 1, 4) AS long)) BETWEEN {1} AND {2}",
                birthDayPath, minAge, maxAge
        );
    }
}
