package OneQ.OnSurvey.domain.survey.service;

import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SurveyQueryService implements SurveyQuery {
    private final SurveyRepository surveyRepository;

    @Override
    public Survey getSurvey(Long surveyId) {
        return surveyRepository.getSurveyById(surveyId);
    }

    @Override
    public List<Survey> getSurveyListByMemberId(Long memberId) {
        return surveyRepository.getSurveyListByMemberId(memberId);
    }

    @Override
    public List<Survey> getSurveyList() {
        return surveyRepository.getSurveyList();
    }
}
