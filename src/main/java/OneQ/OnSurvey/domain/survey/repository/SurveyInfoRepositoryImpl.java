package OneQ.OnSurvey.domain.survey.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public abstract class SurveyInfoRepositoryImpl implements SurveyInfoRepository {
    private final SurveyInfoJpaRepository surveyInfoJpaRepository;
}
