package OneQ.OnSurvey.domain.survey.repository.screening;

import OneQ.OnSurvey.domain.survey.entity.Screening;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.domain.survey.model.dto.ScreeningFormData;
import OneQ.OnSurvey.domain.survey.model.dto.ScreeningIntroData;
import OneQ.OnSurvey.domain.survey.model.dto.ScreeningViewData;
import OneQ.OnSurvey.global.common.util.QuerydslUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.util.List;

import static OneQ.OnSurvey.domain.participation.entity.QResponse.response;
import static OneQ.OnSurvey.domain.participation.entity.QScreeningAnswer.screeningAnswer;
import static OneQ.OnSurvey.domain.survey.entity.QScreening.screening;
import static OneQ.OnSurvey.domain.survey.entity.QSurvey.survey;

@Repository
@RequiredArgsConstructor
public class ScreeningRepositoryImpl implements ScreeningRepository {

    private final ScreeningJpaRepository screeningJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Screening getScreeningBySurveyId(Long surveyId) {
        return screeningJpaRepository.getScreeningBySurveyId(surveyId);
    }

    @Override
    public Slice<ScreeningIntroData> getScreeningSliceByFilters(
        Long lastSurveyId, Pageable pageable, SurveyStatus status, Long creatorId
    ) {

        BooleanBuilder screenedCondition = new BooleanBuilder();
        screenedCondition.and(
            response.isScreened.isNull() // 스크리닝 퀴즈 응답이 없는 설문
        );
        BooleanBuilder surveyCondition = new BooleanBuilder();
        surveyCondition.and(
            survey.status.eq(status)
        );
        surveyCondition.and(
            survey.memberId.ne(creatorId) // 본인이 생성한 설문 제외
        );
        surveyCondition.and(
            survey.id.gt(lastSurveyId) // 마지막으로 조회한 설문 이후의 설문
        );

        List<ScreeningIntroData> results = jpaQueryFactory.select(Projections.constructor(ScreeningIntroData.class,
            screening.id,
            screening.surveyId,
            screening.content,
            screening.answer,
            screeningAnswer.answerId.count()
        ))
            .from(screening)
            .leftJoin(screeningAnswer).on(screening.id.eq(screeningAnswer.screeningId))
            .leftJoin(response).on(
                screening.surveyId.eq(response.surveyId),
                response.memberId.eq(creatorId)
            )
            .leftJoin(survey).on(screening.surveyId.eq(survey.id))
            .where(screenedCondition, surveyCondition)
            .groupBy(screening.id)
            .orderBy(QuerydslUtils.getSortPaidFirst(pageable, survey, survey.isFree))
            .limit(pageable.getPageSize() + 1)
            .fetch();

        return QuerydslUtils.createSlice(results, pageable);
    }

    @Override
    public ScreeningFormData getScreeningFormDataBySurveyId(Long surveyId) {
        return jpaQueryFactory.select(Projections.constructor(ScreeningFormData.class,
            screening.id,
            screening.content
        ))
            .from(screening)
            .where(screening.surveyId.eq(surveyId))
            .fetchOne();
    }

    @Override
    public ScreeningViewData getScreeningIntroBySurveyId(Long surveyId) {
        return jpaQueryFactory.select(Projections.fields(ScreeningViewData.class,
            screening.id.as("screeningId"),
            screening.content,
            screening.answer
        ))
            .from(screening)
            .where(screening.surveyId.eq(surveyId))
            .fetchFirst();
    }

    @Override
    public ScreeningIntroData getScreeningIntroDataByScreeningId(Long screeningId) {
        return jpaQueryFactory.select(Projections.constructor(ScreeningIntroData.class,
            screening.id,
            screening.surveyId,
            screening.content,
            screening.answer,
            screeningAnswer.answerId.count()
        ))
            .from(screening)
            .leftJoin(screeningAnswer).on(screening.id.eq(screeningAnswer.screeningId))
            .where(screening.id.eq(screeningId))
            .groupBy(screening.id)
            .fetchOne();
    }

    @Override
    public Boolean getScreeningAnswer(Long screeningId) {
        return jpaQueryFactory.select(screening.answer)
            .from(screening)
            .where(screening.id.eq(screeningId))
            .fetchOne();
    }

    @Override
    public Long getSurveyId(Long screeningId) {
        return jpaQueryFactory.select(screening.surveyId)
            .from(screening)
            .where(screening.id.eq(screeningId))
            .fetchOne();
    }

    @Override
    public Screening save(Screening screening) {
        return screeningJpaRepository.save(screening);
    }

    @Override
    public void delete(Screening screening) {
        screeningJpaRepository.delete(screening);
    }
}
