package OneQ.OnSurvey.domain.participation.repository.answer;

import OneQ.OnSurvey.domain.participation.model.dto.AnswerStats;

import java.util.Collection;
import java.util.List;

public interface AnswerRepository<E> {
    default E getAnswerByQuestionIdAndMemberId(Long id, Long memberId) {
        return null;
    }
    List<E> getAnswersByQuestionIdListAndMemberId(List<Long> idList, Long memberId);

    E save(E answer);
    List<E> saveAll(Collection<E> answers);

    List<AnswerStats> getAggregatedAnswersByQuestionIds(List<Long> questionIdList);
    default List<AnswerStats> getAnswersByQuestionIds(List<Long> questionIdList){
        return List.of();
    }
}
