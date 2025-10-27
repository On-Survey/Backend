package OneQ.OnSurvey.domain.survey.repository;

import OneQ.OnSurvey.domain.survey.entity.Survey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SurveyRepositoryImpl implements SurveyRepository {
    private final SurveyJpaRepository surveyJpaRepository;

    @Override
    public Survey getSurveyById(Long surveyId) {
        return surveyJpaRepository.getSurveyById(surveyId);
    }

    @Override
    public List<Survey> getSurveyListByMemberId(Long memberId) {
        return surveyJpaRepository.getSurveysByMemberId(memberId);
    }

    // TODO Page<Survey> 반환하도록
    @Override
    public List<Survey> getSurveyList() {
        return surveyJpaRepository.findAll();
    }

    @Override
    public Survey save(Survey survey) {
        return surveyJpaRepository.save(survey);
    }

    @Override
    public void deleteById(Long surveyId) {
        surveyJpaRepository.deleteById(surveyId);
    }
}
