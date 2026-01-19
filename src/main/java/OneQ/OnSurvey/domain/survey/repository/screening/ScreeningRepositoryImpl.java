package OneQ.OnSurvey.domain.survey.repository.screening;

import OneQ.OnSurvey.domain.survey.entity.Screening;
import OneQ.OnSurvey.domain.survey.model.dto.ScreeningFormData;
import OneQ.OnSurvey.domain.survey.model.dto.ScreeningIntroData;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static OneQ.OnSurvey.domain.participation.entity.QResponse.response;
import static OneQ.OnSurvey.domain.survey.entity.QScreening.screening;
import static OneQ.OnSurvey.domain.participation.entity.QScreeningAnswer.screeningAnswer;

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
    public List<ScreeningIntroData> getScreeningListBySurveyIdList(List<Long> surveyIdList) {
        if (surveyIdList == null || surveyIdList.isEmpty()) {
            return List.of();
        }

        return jpaQueryFactory.select(Projections.constructor(ScreeningIntroData.class,
            screening.id,
            screening.surveyId,
            screening.content,
            screening.answer,
            screeningAnswer.answerId.count()
        ))
            .from(screening)
            .leftJoin(screeningAnswer).on(screening.id.eq(screeningAnswer.screeningId))
            .where(
                screening.surveyId.goe(surveyIdList.getFirst()),
                screening.surveyId.in(surveyIdList)
            )
            .groupBy(screening.id)
            .orderBy(screening.surveyId.asc())
            .fetch();
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

    /* 스크리닝 퀴즈에 답변 필요 여부 판단 */
    @Override
    public boolean isScreenRequired(Long surveyId, Long memberId) {
        Tuple isScreened = jpaQueryFactory.select(screening.id, response.isScreened)
            .from(screening)
            .leftJoin(response)
                .on(
                    screening.surveyId.eq(response.surveyId),
                    response.memberId.eq(memberId)
                )
            .where(
                screening.surveyId.eq(surveyId)
            )
            .fetchOne();

        // 스크리닝 퀴즈가 있으나 응답이 없는 케이스 (퀴즈가 없으면 false 반환)
        return isScreened != null
            && isScreened.get(response.isScreened) == null;
    }

    @Override
    public Screening save(Screening screening) {
        return screeningJpaRepository.save(screening);
    }
}
