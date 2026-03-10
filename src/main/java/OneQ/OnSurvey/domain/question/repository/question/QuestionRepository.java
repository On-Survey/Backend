package OneQ.OnSurvey.domain.question.repository.question;

import OneQ.OnSurvey.domain.question.entity.Question;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface QuestionRepository {
    List<Question> getQuestionListBySurveyId(Long surveyId);
    List<Question> getQuestionListBySurveyIdAndSection(Long surveyId, Integer section);
    Question save(Question question);
    List<Question> saveAll(Collection<Question> questions);

    Long getSurveyId(Long questionId);
    void deleteAll(Set<Long> idList);
    void deleteBySurveyIdAndNotInOrder(Long surveyId, Collection<Integer> order);
    int countBySurveyId(Long surveyId);
}
