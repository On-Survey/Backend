package OneQ.OnSurvey.domain.participation.repository.response;

import OneQ.OnSurvey.domain.participation.entity.Response;
import OneQ.OnSurvey.domain.survey.model.AgeRange;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.Residence;
import OneQ.OnSurvey.domain.survey.model.SurveyResponseFilterCondition;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static OneQ.OnSurvey.domain.member.QMember.member;
import static OneQ.OnSurvey.domain.participation.entity.QResponse.response;

@Repository
@RequiredArgsConstructor
public class ResponseRepositoryImpl implements ResponseRepository {

    private final ResponseJpaRepository responseJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Integer getResponseCountBySurveyId(Long surveyId) {
        Long cnt = jpaQueryFactory
                .select(response.count())
                .from(response)
                .where(response.surveyId.eq(surveyId))
                .fetchOne();
        return cnt == null ? 0 : cnt.intValue();
    }

    @Override
    public Integer getResponseCountBySurveyId(
            Long surveyId,
            SurveyResponseFilterCondition filter
    ) {
        SurveyResponseFilterCondition f =
                (filter == null ? SurveyResponseFilterCondition.empty() : filter);

        Long cnt = jpaQueryFactory
                .select(response.count())
                .from(response)
                .join(member).on(member.id.eq(response.memberId))
                .where(
                        response.surveyId.eq(surveyId),
                        buildAgeCondition(member.birthDay, f.ages()),
                        buildGenderCondition(member.gender, f.genders()),
                        buildResidenceCondition(member.residence, f.residences())
                )
                .fetchOne();

        return cnt == null ? 0 : cnt.intValue();
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
    public List<Long> getExcludedSurveyIdList(Long memberId, boolean checkScreened) {
        BooleanBuilder statusBuilder = new BooleanBuilder();

        statusBuilder.or(response.isResponded.eq(true));

        if (checkScreened) {
            statusBuilder.or(response.isScreened.eq(true));
        }

        return jpaQueryFactory
                .select(response.surveyId)
                .from(response)
                .where(
                        response.memberId.eq(memberId)
                                .and(statusBuilder)
                )
                .fetch();
    }

    @Override
    public Optional<Response> findBySurveyIdAndMemberId(Long surveyId, Long memberId) {
        return responseJpaRepository.findBySurveyIdAndMemberId(surveyId, memberId);
    }

    /* 설문 응답 완료 여부 판단 */
    @Override
    public boolean isSurveyResponded(Long surveyId, Long memberId) {
        Boolean isResponded = jpaQueryFactory.select(response.isResponded)
            .from(response)
            .where(
                response.surveyId.eq(surveyId),
                response.memberId.eq(memberId)
            )
            .fetchOne();

        // 응답 기록이 있고, 설문 응답이 완료인 케이스
        return isResponded != null && isResponded;
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
