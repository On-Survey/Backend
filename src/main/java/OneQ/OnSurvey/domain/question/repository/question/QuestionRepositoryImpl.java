package OneQ.OnSurvey.domain.question.repository.question;

import OneQ.OnSurvey.domain.question.entity.Question;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static OneQ.OnSurvey.domain.question.entity.QQuestion.question;

@Repository
@RequiredArgsConstructor
public class QuestionRepositoryImpl implements QuestionRepository {

    private final QuestionJpaRepository questionJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Question> getQuestionListBySurveyId(Long surveyId) {
        return questionJpaRepository.getQuestionsBySurveyIdOrderByOrder(surveyId);
    }

    @Override
    public Question save(Question question) {
        return questionJpaRepository.save(question);
    }

    @Override
    public List<Question> saveAll(Collection<Question> questions) {
        return questionJpaRepository.saveAllAndFlush(questions);
    }

    @Override
    public Long getSurveyId(Long questionId) {
        return jpaQueryFactory.select(question.surveyId)
            .from(question)
            .where(question.questionId.eq(questionId))
            .fetchOne();
    }

    @Override
    public void deleteAll(Set<Long> idList) {
        questionJpaRepository.deleteAllByIdInBatch(idList);
    }
}
