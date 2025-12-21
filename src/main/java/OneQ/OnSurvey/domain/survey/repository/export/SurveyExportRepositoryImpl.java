package OneQ.OnSurvey.domain.survey.repository.export;

import OneQ.OnSurvey.domain.survey.model.export.SurveyAnswerProjection;
import OneQ.OnSurvey.domain.survey.model.export.SurveyMemberProjection;
import OneQ.OnSurvey.domain.survey.model.export.SurveyQuestionHeader;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static OneQ.OnSurvey.domain.member.QMember.member;
import static OneQ.OnSurvey.domain.participation.entity.QQuestionAnswer.questionAnswer;
import static OneQ.OnSurvey.domain.question.entity.QQuestion.question;
import static OneQ.OnSurvey.domain.survey.entity.QSurvey.survey;

@Repository
@RequiredArgsConstructor
public class SurveyExportRepositoryImpl implements SurveyExportRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<SurveyQuestionHeader> findQuestionHeaders(Long surveyId) {
        return queryFactory
                .select(Projections.constructor(
                        SurveyQuestionHeader.class,
                        question.questionId,
                        question.order,
                        question.title
                ))
                .from(question)
                .where(question.surveyId.eq(surveyId))
                .orderBy(question.order.asc())
                .fetch();
    }

    @Override
    public List<SurveyMemberProjection> findMembersWhoAnswered(Long surveyId) {
        return queryFactory
                .select(Projections.constructor(
                        SurveyMemberProjection.class,
                        member.id,
                        member.birthDay,
                        member.gender.stringValue(),
                        member.residence.stringValue()
                ))
                .from(questionAnswer)
                .join(question).on(question.questionId.eq(questionAnswer.questionId))
                .join(member).on(member.id.eq(questionAnswer.memberId))
                .where(question.surveyId.eq(surveyId))
                .distinct()
                .orderBy(member.id.asc())
                .fetch();
    }

    @Override
    public List<SurveyAnswerProjection> findAnswers(Long surveyId) {
        return queryFactory
                .select(Projections.constructor(
                        SurveyAnswerProjection.class,
                        questionAnswer.memberId,
                        questionAnswer.questionId,
                        questionAnswer.content
                ))
                .from(questionAnswer)
                .join(question).on(question.questionId.eq(questionAnswer.questionId))
                .where(question.surveyId.eq(surveyId))
                .fetch();
    }

    @Override
    public String findSurveyTitle(Long surveyId) {
        return queryFactory
                .select(survey.title)
                .from(survey)
                .where(survey.id.eq(surveyId))
                .fetchOne();
    }
}
