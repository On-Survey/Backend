package OneQ.OnSurvey.domain.survey.repository;


import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SurveyRepository {
    Survey getSurveyById(Long surveyId);
    List<Survey> getSurveyListByMemberId(Long memberId);
    List<Survey> getSurveyList(SurveyStatus status, Long lastSurveyId, Pageable pageable);

    Survey save(Survey survey);
    void deleteById(Long surveyId);
}
