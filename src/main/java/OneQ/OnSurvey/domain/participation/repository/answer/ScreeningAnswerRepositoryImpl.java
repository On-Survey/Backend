package OneQ.OnSurvey.domain.participation.repository.answer;

import OneQ.OnSurvey.domain.participation.entity.ScreeningAnswer;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerStats;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

import static OneQ.OnSurvey.domain.participation.entity.QScreeningAnswer.screeningAnswer;
import static OneQ.OnSurvey.domain.survey.entity.QScreening.screening;

@Repository
public class ScreeningAnswerRepositoryImpl
        extends AbstractAnswerRepository<ScreeningAnswer>
        implements ScreeningAnswerRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public ScreeningAnswerRepositoryImpl(
        AnswerJpaRepository<ScreeningAnswer> answerJpaRepository,
        JPAQueryFactory jpaQueryFactory
    ) {
        super(answerJpaRepository);
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public ScreeningAnswer save(ScreeningAnswer answer) {
        return answerJpaRepository.save(answer);
    }

    @Override
    public List<AnswerStats> getAggregatedAnswersByQuestionIds(List<Long> screeningIdList) {
        return jpaQueryFactory.select(Projections.constructor(AnswerStats.class,
                screeningAnswer.screeningId,
                screeningAnswer.content,
                screeningAnswer.answerId.count()
            ))
            .from(screeningAnswer)
            .where(screeningAnswer.screeningId.in(screeningIdList))
            .groupBy(screeningAnswer.screeningId, screeningAnswer.content)
            .orderBy(screeningAnswer.screeningId.asc())
            .fetch();
    }

    @Override
    public List<Long> findAnsweredSurveyIds(Long memberId) {
        return jpaQueryFactory
                .select(screening.surveyId)
                .distinct()
                .from(screeningAnswer)
                .join(screening).on(screeningAnswer.screeningId.eq(screening.id))
                .where(
                        screeningAnswer.memberId.eq(memberId)
                )
                .fetch();
    }
}
