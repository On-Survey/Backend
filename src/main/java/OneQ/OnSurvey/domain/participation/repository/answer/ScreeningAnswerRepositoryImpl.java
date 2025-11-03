package OneQ.OnSurvey.domain.participation.repository.answer;

import OneQ.OnSurvey.domain.participation.entity.ScreeningAnswer;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

import static OneQ.OnSurvey.domain.participation.entity.QScreeningAnswer.screeningAnswer;

@Repository
public class ScreeningAnswerRepositoryImpl extends AbstractAnswerRepository<ScreeningAnswer> {
    private final JPAQueryFactory jpaQueryFactory;

    public ScreeningAnswerRepositoryImpl(
        AnswerJpaRepository<ScreeningAnswer> answerJpaRepository,
        JPAQueryFactory jpaQueryFactory
    ) {
        super(answerJpaRepository);
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public ScreeningAnswer getAnswerByQuestionIdAndMemberId(Long screeningId, Long memberId) {
        return jpaQueryFactory.selectFrom(screeningAnswer)
            .where(
                screeningAnswer.screeningId.eq(screeningId),
                screeningAnswer.memberId.eq(memberId)
            )
            .fetchOne();
    }

    @Override
    public List<ScreeningAnswer> getAnswersByQuestionIdListAndMemberId(List<Long> screeningIdList, Long memberId) {
        return jpaQueryFactory.selectFrom(screeningAnswer)
            .where(
                screeningAnswer.screeningId.in(screeningIdList),
                screeningAnswer.memberId.eq(memberId)
            )
            .fetch();
    }

    public ScreeningAnswer save(ScreeningAnswer answer) {
        return answerJpaRepository.save(answer);
    }
}
