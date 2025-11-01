package OneQ.OnSurvey.domain.survey.repository.screening;

import OneQ.OnSurvey.domain.survey.entity.Screening;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ScreeningRepositoryImpl implements ScreeningRepository {
    private final ScreeningJpaRepository screeningJpaRepository;

    @Override
    public Screening getScreeningBySurveyId(Long surveyId) {
        return screeningJpaRepository.getScreeningBySurveyId(surveyId);
    }

    @Override
    public Screening save(Screening screening) {
        return screeningJpaRepository.save(screening);
    }
}
