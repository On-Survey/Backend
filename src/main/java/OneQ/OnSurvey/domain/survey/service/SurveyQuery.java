package OneQ.OnSurvey.domain.survey.service;

import OneQ.OnSurvey.domain.survey.entity.Survey;

import java.util.List;

public interface SurveyQuery {
    Survey getSurvey(Long surveyId);
    List<Survey> getSurveyListByMemberId(Long memberId); // dueCount + answerCount 함께
    List<Survey> getSurveyList();
}
