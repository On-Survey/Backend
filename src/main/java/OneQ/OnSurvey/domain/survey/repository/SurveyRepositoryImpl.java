package OneQ.OnSurvey.domain.survey.repository;

import OneQ.OnSurvey.domain.member.dto.MemberSegmentation;
import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.model.AgeRange;
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
import static OneQ.OnSurvey.domain.survey.entity.QScreening.screening;

import static OneQ.OnSurvey.domain.participation.entity.QScreeningAnswer.screeningAnswer;

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

    @Override
    public SurveyStatus getSurveyStatusById(Long surveyId) {
        return jpaQueryFactory
            .select(survey.status)
            .from(survey)
            .where(survey.id.eq(surveyId))
            .fetchOne();
    }
}
