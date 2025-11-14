package OneQ.OnSurvey.domain.question.repository.question;

import OneQ.OnSurvey.domain.question.entity.Question;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface QuestionRepository {
    List<Question> getQuestionListBySurveyId(Long surveyId);
    Question getQuestionById(Long questionId);
    List<Question> getQuestionsByIds(Collection<Long> questionIdList);

    Question save(Question question);
    List<Question> saveAll(Collection<Question> questions);

    Boolean deleteAll(Set<Long> idList);
}
