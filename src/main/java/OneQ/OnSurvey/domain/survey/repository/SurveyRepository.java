package OneQ.OnSurvey.domain.survey.repository;


import OneQ.OnSurvey.domain.survey.entity.Survey;

import java.util.List;

public interface SurveyRepository {
    Survey getSurveyById(Long surveyId);
    List<Survey> getSurveyListByMemberId(Long memberId);
    List<Survey> getSurveyList();

    Survey save(Survey survey);
    void deleteById(Long surveyId);
}
