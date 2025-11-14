package OneQ.OnSurvey.domain.survey.repository;


import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Optional;

public interface SurveyRepository {
    Optional<Survey> getSurveyById(Long surveyId);
    List<Survey> getSurveyListByMemberId(Long memberId);
    Slice<Survey> getSurveyListByStatus(SurveyStatus status, Long lastSurveyId, Pageable pageable);
    Slice<Survey> getSurveyList(Long lastSurveyId, Pageable pageable);

    Survey save(Survey survey);
    void deleteById(Long surveyId);
}
