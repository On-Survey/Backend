package OneQ.OnSurvey.domain.question.repository.question;

import OneQ.OnSurvey.domain.question.entity.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class QuestionRepositoryImpl implements QuestionRepository {
    private final QuestionJpaRepository questionJpaRepository;

    @Override
    public List<Question> getQuestionListBySurveyId(Long surveyId) {
        return questionJpaRepository.getQuestionsBySurveyId(surveyId);
    }

    @Override
    public Question getQuestionById(Long questionId) {
        return questionJpaRepository.getQuestionByQuestionId(questionId);
    }

    @Override
    public List<Question> getQuestionsByIds(Collection<Long> questionIdList) {
        return questionJpaRepository.getQuestionsByQuestionIdIn(questionIdList);
    }

    @Override
    public Question save(Question question) {
        return questionJpaRepository.save(question);
    }

    @Override
    public List<Question> saveAll(Collection<Question> questions) {
        return questionJpaRepository.saveAll(questions);
    }
}
