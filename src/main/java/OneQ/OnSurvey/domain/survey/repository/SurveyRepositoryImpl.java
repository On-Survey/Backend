package OneQ.OnSurvey.domain.survey.repository;

import OneQ.OnSurvey.domain.member.dto.MemberSegmentation;
import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.participation.model.dto.ParticipationStatus;
import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.model.AgeRange;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.domain.survey.model.dto.SurveyDetailData;
import OneQ.OnSurvey.domain.survey.model.dto.SurveyListView;
import OneQ.OnSurvey.domain.survey.model.dto.SurveySearchQuery;
import OneQ.OnSurvey.global.common.util.QuerydslUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public Slice<Survey> getSurveyListByFilters(
        Long lastSurveyId, LocalDateTime lastDeadline, Pageable pageable,
        SurveyStatus status, Long memberId, Collection<Long> excludedIds, MemberSegmentation memberSegmentation,
        boolean filterByScreeningAnswer
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

        AgeRange memberAgeRange = memberSegmentation.convertBirthDayIntoAgeRange();

        builder.and(
            surveyInfo.ages.contains(AgeRange.ALL)
            .or(surveyInfo.ages.contains(memberAgeRange))
        );
        builder.and(
            surveyInfo.gender.eq(Gender.ALL)
            .or(surveyInfo.gender.eq(memberSegmentation.getGender()))
        );
        if (filterByScreeningAnswer) {
            builder.and(
                screening.id.isNull()
                .or(
                    screeningAnswer.answerId.isNotNull()
                    .and(screeningAnswer.content.eq(screening.answer))
                )
            );
        }

        List<Survey> surveyList = jpaQueryFactory.selectFrom(survey)
            .distinct()
            .leftJoin(survey.interests).fetchJoin()
            .leftJoin(surveyInfo).on(survey.id.eq(surveyInfo.surveyId))
            .leftJoin(screening).on(survey.id.eq(screening.surveyId))
            .leftJoin(screeningAnswer).on(
                screeningAnswer.memberId.eq(memberId).and(screening.id.eq(screeningAnswer.screeningId)))
            .where(builder)
            .orderBy(QuerydslUtils.getSortPaidFirst(pageable, survey, survey.isFree))
            .limit(pageable.getPageSize() + 1)
            .fetch();

        return createSlice(surveyList, pageable);
    }

    @Override
    public Page<SurveyListView> getPagedSurveyListViewByQuery(Pageable pageable, SurveySearchQuery query) {
        BooleanBuilder builder = new BooleanBuilder();
        if (query.title() != null) {
            builder.and(survey.title.eq(query.title()));
        }
        if (query.creator() != null) {
            builder.and(survey.memberId.eq(query.creator()));
        }
        if (query.status() != null && !query.status().isEmpty()) {
            builder.and(survey.status.in(query.status()));
        }
        if (query.startDate() != null) {
            builder.and(survey.createdAt.goe(query.startDate().atStartOfDay()));
        }
        if (query.endDate() != null) {
            builder.and(survey.createdAt.loe(query.endDate().atStartOfDay()));
        }

        List<SurveyListView> results = jpaQueryFactory.select(Projections.fields(SurveyListView.class,
                survey.id.as("surveyId"),
                survey.title,
                survey.memberId.as("creator"),
                survey.createdAt,
                survey.status
            ))
            .from(survey)
            .where(builder)
            .orderBy(QuerydslUtils.getSort(pageable, survey))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
            .select(survey.count())
            .from(survey)
            .where(builder);

        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
    }

    @Override
    public SurveyDetailData getSurveyDetailDataById(Long surveyId) {
        EnumPath<Interest> interestAlias = Expressions.enumPath(Interest.class, "interestAlias");
        EnumPath<AgeRange> ageAlias = Expressions.enumPath(AgeRange.class, "ageAlias");

        Map<Long, SurveyDetailData> result = jpaQueryFactory
            .from(survey)
            .leftJoin(survey.interests, interestAlias)
            .leftJoin(surveyInfo).on(survey.id.eq(surveyInfo.surveyId))
            .leftJoin(surveyInfo.ages, ageAlias)
            .where(survey.id.eq(surveyId))
            .transform(
                groupBy(survey.id).as(Projections.fields(SurveyDetailData.class,
                    survey.id.as("surveyId"),
                    survey.title,
                    survey.description,
                    survey.deadline,
                    surveyInfo.dueCount,
                    set(ageAlias).as("ages"),
                    surveyInfo.gender,
                    surveyInfo.residence,
                    set(interestAlias).as("interests")
                ))
            );
        System.out.println("result = " + result);
        return result.get(surveyId);
    }

    @Override
    public Survey save(Survey survey) {
        return surveyJpaRepository.save(survey);
    }

    private Slice<Survey> createSlice(List<Survey> surveyList, Pageable pageable) {
        boolean hasNext = false;
        int size = pageable.getPageSize();

        if (surveyList.size() > size) {
            hasNext = true;
            surveyList.remove(size);
        }

        return new SliceImpl<>(surveyList, pageable, hasNext);
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
}
