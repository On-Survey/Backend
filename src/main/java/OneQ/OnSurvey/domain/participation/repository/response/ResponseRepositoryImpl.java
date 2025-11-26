package OneQ.OnSurvey.domain.participation.repository.response;

import OneQ.OnSurvey.domain.participation.entity.Response;
import OneQ.OnSurvey.domain.survey.model.AgeRange;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.Residence;
import OneQ.OnSurvey.domain.survey.model.SurveyResponseFilterCondition;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static OneQ.OnSurvey.domain.member.QMember.member;
import static OneQ.OnSurvey.domain.participation.entity.QResponse.response;

@Repository
@RequiredArgsConstructor
public class ResponseRepositoryImpl implements ResponseRepository {
    private final ResponseJpaRepository responseJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Response> getResponsesByMemberId(Long memberId) {
        return responseJpaRepository.getResponsesByMemberId(memberId);
    }

    @Override
    public List<Response> getResponsesBySurveyId(Long surveyId) {
        return responseJpaRepository.getResponsesBySurveyId(surveyId);
    }

    @Override
    public Response getResponseBySurveyIdAndMemberId(Long surveyId, Long memberId) {
        return jpaQueryFactory.selectFrom(response)
            .where(
                response.surveyId.eq(surveyId),
                response.memberId.eq(memberId)
            )
            .fetchOne();
    }

    @Override
    public Integer getResponseCountBySurveyId(Long surveyId) {
        return responseJpaRepository.countResponsesBySurveyId(surveyId);
    }

    @Override
    public Map<Long, Long> getResponseCountsBySurveyIds(Collection<Long> surveyIds) {
        return jpaQueryFactory.select(response.surveyId, response.surveyId.count())
            .from(response)
            .where(response.surveyId.in(surveyIds))
            .groupBy(response.surveyId)
            .fetch()
            .stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get(response.surveyId),
                tuple -> tuple.get(response.surveyId.count())
            ));
    }

    @Override
    public Response save(Response response) {
        return responseJpaRepository.save(response);
    }

    @Override
    public boolean existsBySurveyIdAndMemberId(Long surveyId, Long memberId) {
        return responseJpaRepository.existsBySurveyIdAndMemberId(surveyId, memberId);
    }

    @Override
    public Integer getResponseCountBySurveyId(Long surveyId, SurveyResponseFilterCondition filter) {
        Long count = jpaQueryFactory
                .select(response.count())
                .from(response)
                .join(member).on(response.memberId.eq(member.id))
                .where(
                        response.surveyId.eq(surveyId),
                        buildAgeCondition(member.birthDay, filter.ages()),
                        buildGenderCondition(member.gender, filter.genders()),
                        buildResidenceCondition(member.residence, filter.residences())
                )
                .fetchOne();

        return count == null ? 0 : count.intValue();
    }

    private BooleanExpression buildGenderCondition(
            com.querydsl.core.types.dsl.EnumPath<Gender> genderPath,
            List<Gender> genders
    ) {
        if (genders == null || genders.isEmpty()) return null;
        return genderPath.in(genders);
    }

    private BooleanExpression buildResidenceCondition(
            com.querydsl.core.types.dsl.EnumPath<Residence> residencePath,
            List<Residence> residences
    ) {
        if (residences == null || residences.isEmpty()) return null;
        return residencePath.in(residences);
    }

    private BooleanExpression buildAgeCondition(StringPath birthDayPath, List<AgeRange> ages) {
        if (ages == null || ages.isEmpty()) return null;

        BooleanExpression exp = null;
        for (AgeRange range : ages) {
            BooleanExpression one = ageRangeExpr(birthDayPath, range);
            exp = (exp == null) ? one : exp.or(one);
        }
        return exp;
    }

    /**
     * birthDay: "yyyyMMdd" 문자열 기반으로 MySQL TIMESTAMPDIFF로 나이 계산
     */
    private BooleanExpression ageRangeExpr(StringPath birthDayPath, AgeRange range) {
        int minAge;
        int maxAge;

        switch (range) {
            case TEN -> {minAge = 10; maxAge = 19;}
            case TWENTY -> {minAge = 20; maxAge = 29;}
            case THIRTY -> {minAge = 30; maxAge = 39;}
            case FOURTY -> {minAge = 40; maxAge = 49;}
            case FIFTY -> {minAge = 50; maxAge = 59;}
            case SIXTY -> {minAge = 60; maxAge = 69;}
            case OVER -> {minAge = 70; maxAge = 200;}
            default -> {
                return null;
            }
        }

        return Expressions.booleanTemplate(
                "(YEAR(CURDATE()) - CAST(SUBSTRING({0}, 1, 4) AS long)) BETWEEN {1} AND {2}",
                birthDayPath, minAge, maxAge
        );
    }
}
