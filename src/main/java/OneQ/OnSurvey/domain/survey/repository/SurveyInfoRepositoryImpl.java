package OneQ.OnSurvey.domain.survey.repository;

import OneQ.OnSurvey.domain.survey.entity.SurveyInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SurveyInfoRepositoryImpl implements SurveyInfoRepository {

    private final SurveyInfoJpaRepository surveyInfoJpaRepository;

    @Override
    public SurveyInfo save(SurveyInfo surveyInfo) {
        surveyInfoJpaRepository.save(surveyInfo);
        return surveyInfo;
    }

    @Override
    public Optional<SurveyInfo> findBySurveyId(Long surveyId) {
        return surveyInfoJpaRepository.findBySurveyId(surveyId);
    }
}
