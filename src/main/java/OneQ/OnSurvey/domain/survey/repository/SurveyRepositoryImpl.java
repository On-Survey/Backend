package OneQ.OnSurvey.domain.survey.repository;

import OneQ.OnSurvey.domain.member.dto.MemberSegmentation;
import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.participation.model.dto.ParticipationStatus;
import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.model.AgeRange;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.domain.survey.model.dto.SurveyWithEligibility;
import OneQ.OnSurvey.global.common.util.QuerydslUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.set;

import static OneQ.OnSurvey.domain.participation.entity.QResponse.response;
import static OneQ.OnSurvey.domain.participation.entity.QScreeningAnswer.screeningAnswer;
import static OneQ.OnSurvey.domain.survey.entity.QScreening.screening;
import static OneQ.OnSurvey.domain.survey.entity.QSurvey.survey;
import static OneQ.OnSurvey.domain.survey.entity.QSurveyInfo.surveyInfo;

@Repository
@RequiredArgsConstructor
public class SurveyRepositoryImpl implements SurveyRepository {

    private final SurveyJpaRepository surveyJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Survey> getSurveyById(Long surveyId) {
        Survey result = jpaQueryFactory
                .selectFrom(survey)
                .distinct()
                .leftJoin(survey.interests).fetchJoin()
                .where(survey.id.eq(surveyId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<Survey> getSurveyListByMemberId(Long memberId) {
        return surveyJpaRepository.getSurveysByMemberId(memberId);
    }

    @Override
    public Slice<SurveyWithEligibility> getSurveyListWithEligibility(
        Long lastSurveyId, LocalDateTime lastDeadline, Pageable pageable,
        SurveyStatus status, Long memberId, Collection<Long> excludedIds, MemberSegmentation memberSegmentation
    ) {
        List<Long> targetIdList = getSurveyIdListByFilters(lastSurveyId, lastDeadline, pageable, status, memberId, excludedIds);

        AgeRange memberAgeRange = memberSegmentation.convertBirthDayIntoAgeRange();
        BooleanExpression condition = (
                surveyInfo.ages.contains(AgeRange.ALL).or(surveyInfo.ages.contains(memberAgeRange))
            ).and(
                surveyInfo.gender.eq(Gender.ALL).or(surveyInfo.gender.eq(memberSegmentation.getGender()))
            );
        Expression<Boolean> isEligible = new CaseBuilder()
            .when(condition).then(true)
            .otherwise(false)
            .as("isEligible");

        EnumPath<Interest> interestAlias = Expressions.enumPath(Interest.class, "interestAlias");
        EnumPath<AgeRange> ageAlias = Expressions.enumPath(AgeRange.class, "ageAlias");

        List<SurveyWithEligibility> results = new ArrayList<>(jpaQueryFactory
            .from(survey)
            .distinct()
            .leftJoin(survey.interests, interestAlias)
            .leftJoin(surveyInfo).on(survey.id.eq(surveyInfo.surveyId))
            .leftJoin(surveyInfo.ages, ageAlias)
            .leftJoin(screening).on(survey.id.eq(screening.surveyId))
            .leftJoin(screeningAnswer).on(
                screeningAnswer.memberId.eq(memberId).and(screening.id.eq(screeningAnswer.screeningId)))
            .where(survey.id.in(targetIdList))
            .orderBy(QuerydslUtils.getSortPaidFirst(pageable, survey, survey.isFree))
            .transform(groupBy(survey.id).as(Projections.fields(SurveyWithEligibility.class,
                survey.id.as("surveyId"),
                survey.memberId,
                survey.title,
                survey.description,
                survey.isFree,
                set(interestAlias).as("interests"),
                survey.deadline,
                isEligible
            )))
            .values());

        return QuerydslUtils.createSlice(results, pageable);
    }

    @Override
    public Survey save(Survey survey) {
        return surveyJpaRepository.save(survey);
    }

    @Override
    public SurveyStatus getSurveyStatusById(Long surveyId) {
        return jpaQueryFactory
            .select(survey.status)
            .from(survey)
            .where(survey.id.eq(surveyId))
            .fetchOne();
    }

    @Override
    public ParticipationStatus getParticipationStatus(Long surveyId, Long memberId) {
        Tuple statusResult = jpaQueryFactory
            .select(
                screening.id,           // 스크리닝 존재 여부
                response.isScreened,    // 스크리닝 응답 여부
                response.isResponded    // 설문 응답 여부
            )
            .from(survey)
            .leftJoin(screening).on(
                survey.id.eq(screening.surveyId)
            )
            .leftJoin(response).on(
                survey.id.eq(response.surveyId),
                response.memberId.eq(memberId)
            )
            .where(survey.id.eq(surveyId))
            .fetchOne();

        if (statusResult == null) {
            return ParticipationStatus.defaultStatus(false);
        }

        Long screeningId = statusResult.get(screening.id);
        Boolean isScreened =  statusResult.get(response.isScreened);
        Boolean isResponded = statusResult.get(response.isResponded);

        return ParticipationStatus.generateStatus(screeningId, isScreened, isResponded);
    }

    @Override
    public List<Long> getSurveyIdListByFilters(
        Long lastSurveyId, LocalDateTime lastDeadline, Pageable pageable,
        SurveyStatus status, Long memberId, Collection<Long> excludedIds
    ) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(
            survey.status.eq(status)
        );

        if (lastDeadline == null) {
            builder
                .and(survey.id.gt(lastSurveyId))
                .and(survey.deadline.goe(LocalDateTime.now()));
        } else {
            builder.and(
                survey.deadline.gt(lastDeadline)
                    .or(survey.deadline.eq(lastDeadline)
                        .and(survey.id.gt(lastSurveyId)
                        )
                    )
            );
        }

        if (!excludedIds.isEmpty()) {
            builder.and(survey.id.notIn(excludedIds));
        }
        if (memberId != null) {
            builder.and(survey.memberId.ne(memberId));
        }

        // interest, age로 인해 row가 뻥튀기되어 LIMIT만큼 조회가 되지 않으므로, LIMIT을 survey에 적용시키기 위해 설문ID를 먼저 조회
        return jpaQueryFactory
            .select(survey.id)
            .from(survey)
            .leftJoin(surveyInfo).on(survey.id.eq(surveyInfo.surveyId))
            .leftJoin(screening).on(survey.id.eq(screening.surveyId))
            .leftJoin(screeningAnswer).on(
                screeningAnswer.memberId.eq(memberId)
                    .and(screening.id.eq(screeningAnswer.screeningId))
            )
            .where(builder)
            .orderBy(QuerydslUtils.getSortPaidFirst(pageable, survey, survey.isFree))
            .limit(pageable.getPageSize() + 1)
            .fetch();
    }
}
