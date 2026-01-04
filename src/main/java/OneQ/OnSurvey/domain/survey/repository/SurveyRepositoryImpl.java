package OneQ.OnSurvey.domain.survey.repository;

import OneQ.OnSurvey.domain.member.dto.MemberSegmentation;
import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.global.common.util.QuerydslUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
        SurveyStatus status, Long creatorId, Collection<Long> excludedIds, MemberSegmentation memberSegmentation
    ) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(
            survey.status.eq(status)
        );

        if (lastDeadline == null) {
            // StringTemplate deadlineTemplate = QuerydslUtils.convertLocalDateTimeIntoStringTemplate(LocalDateTime.now());
            builder
                .and(survey.id.gt(lastSurveyId))
                .and(survey.deadline.goe(LocalDateTime.now()));
        } else {
            // StringTemplate deadlineTemplate = QuerydslUtils.convertLocalDateTimeIntoStringTemplate(lastDeadline);
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
        if (creatorId != null) {
            builder.and(survey.memberId.ne(creatorId));
        }

        builder.and(surveyInfo.ages.contains(memberSegmentation.convertBirthDayIntoAgeRange()));
        builder.and(
            surveyInfo.gender.eq(Gender.ALL).or(surveyInfo.gender.eq(memberSegmentation.getGender()))
        );

        List<Survey> surveyList = jpaQueryFactory.selectFrom(survey)
            .leftJoin(survey.interests).fetchJoin()
            .leftJoin(surveyInfo).on(survey.id.eq(surveyInfo.surveyId)).fetchJoin()
            .leftJoin(surveyInfo.ages)
            .where(builder)
            .orderBy(QuerydslUtils.getSort(pageable, survey))
            .limit(pageable.getPageSize() + 1)
            .fetch();

        return createSlice(surveyList, pageable);
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
}
