package OneQ.OnSurvey.domain.participation.repository.answer;

import OneQ.OnSurvey.domain.participation.entity.QuestionAnswer;
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
}
