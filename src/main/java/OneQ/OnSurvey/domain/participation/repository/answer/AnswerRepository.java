package OneQ.OnSurvey.domain.participation.repository.answer;

import OneQ.OnSurvey.domain.participation.model.dto.AnswerStats;
import OneQ.OnSurvey.domain.survey.SurveyErrorCode;
import OneQ.OnSurvey.domain.survey.model.SurveyResponseFilterCondition;
import OneQ.OnSurvey.global.common.exception.CustomException;

import java.util.Collection;
import java.util.List;

public interface AnswerRepository<E> {
    E save(E answer);
    List<E> saveAll(Collection<E> answers);
    void deleteAllByIds(Collection<Long> answers);

    default List<E> getAnswerListByQuestionIdsAndMemberId(Collection<Long> questionIds, Long memberId) {
        return List.of();
    }

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

    default List<AnswerStats> getRespondentCountsByQuestionIds(
            List<Long> questionIds,
            SurveyResponseFilterCondition filter
    ) {
        return List.of();
    }
    default void deleteInvalidSectionQuestionAnswer(long surveyId, long memberId, Collection<Integer> validSectionList) {
        throw new CustomException(SurveyErrorCode.SURVEY_PARTICIPATION_INVALID_SECTION_REMAIN);
    }
}
