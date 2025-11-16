package OneQ.OnSurvey.domain.participation.repository.answer;

import OneQ.OnSurvey.domain.participation.entity.QuestionAnswer;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerStats;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

import static OneQ.OnSurvey.domain.participation.entity.QQuestionAnswer.questionAnswer;

@Repository
public class QuestionAnswerRepositoryImpl extends AbstractAnswerRepository<QuestionAnswer> {
    private final JPAQueryFactory jpaQueryFactory;

    public QuestionAnswerRepositoryImpl(
        AnswerJpaRepository<QuestionAnswer> answerJpaRepository,
        JPAQueryFactory jpaQueryFactory
    ) {
        super(answerJpaRepository);
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public List<QuestionAnswer> getAnswersByQuestionIdListAndMemberId(List<Long> questionIdList, Long memberId) {
        return jpaQueryFactory.selectFrom(questionAnswer)
            .where(
                questionAnswer.questionId.in(questionIdList),
                questionAnswer.memberId.eq(memberId)
            )
            .fetch();
    }

    @Override
    public List<AnswerStats> getAggregatedAnswersByQuestionIds(List<Long> questionIdList) {
        return jpaQueryFactory.select(Projections.constructor(AnswerStats.class,
                questionAnswer.questionId,
                questionAnswer.content,
                questionAnswer.answerId.count()
            ))
            .from(questionAnswer)
            .where(questionAnswer.questionId.in(questionIdList))
            .groupBy(questionAnswer.questionId, questionAnswer.content)
            .orderBy(questionAnswer.questionId.asc())
            .fetch();
    }

    @Override
    public List<AnswerStats> getAnswersByQuestionIds(List<Long> questionIdList) {
        return jpaQueryFactory.select(Projections.constructor(AnswerStats.class,
                questionAnswer.questionId,
                questionAnswer.content
            ))
            .from(questionAnswer)
            .where(questionAnswer.questionId.in(questionIdList))
            .orderBy(questionAnswer.questionId.asc())
            .fetch();
    }
}
