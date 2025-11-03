package OneQ.OnSurvey.domain.participation.service.answer;

import OneQ.OnSurvey.domain.participation.entity.AbstractAnswer;

import java.util.List;

public interface AnswerQuery<E extends AbstractAnswer> {
    List<E> getAnswersByIdListAndMemberId(List<Long> idList, Long memberId);
    default E getAnswerById(Long id) {
        return null;
    }
}
