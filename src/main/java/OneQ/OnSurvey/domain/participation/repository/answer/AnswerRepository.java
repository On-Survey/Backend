package OneQ.OnSurvey.domain.participation.repository.answer;

import OneQ.OnSurvey.domain.participation.model.dto.AnswerStats;
import OneQ.OnSurvey.domain.survey.model.SurveyResponseFilterCondition;

import java.util.Collection;
import java.util.List;

public interface AnswerRepository<E> {
    E save(E answer);
    List<E> saveAll(Collection<E> answers);

    List<AnswerStats> getAggregatedAnswersByQuestionIds(List<Long> questionIdList);
    default List<AnswerStats> getAnswersByQuestionIds(List<Long> questionIdList){
        return List.of();
    }

    default List<AnswerStats> getAggregatedAnswersByQuestionIds(
            List<Long> questionIds,
            SurveyResponseFilterCondition filter
    ) {
        return getAggregatedAnswersByQuestionIds(questionIds);
    }

    default List<AnswerStats> getAnswersByQuestionIds(
            List<Long> questionIds,
            SurveyResponseFilterCondition filter
    ) {
        return getAnswersByQuestionIds(questionIds);
    }
}
