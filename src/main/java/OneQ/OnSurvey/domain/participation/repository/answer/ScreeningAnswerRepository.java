package OneQ.OnSurvey.domain.participation.repository.answer;

import OneQ.OnSurvey.domain.participation.entity.ScreeningAnswer;

import java.util.List;

public interface ScreeningAnswerRepository extends AnswerRepository<ScreeningAnswer> {
    List<Long> findAnsweredSurveyIds(Long memberId);
}
