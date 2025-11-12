package OneQ.OnSurvey.domain.survey.repository.screening;

import OneQ.OnSurvey.domain.survey.entity.Screening;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ScreeningRepositoryImpl implements ScreeningRepository {
    private final ScreeningJpaRepository screeningJpaRepository;

    @Override
    public Screening getScreeningBySurveyId(Long surveyId) {
        return screeningJpaRepository.getScreeningBySurveyId(surveyId);
    }

    @Override
    public List<Screening> getScreeningListBySurveyIdList(List<Long> surveyIdList) {
        return screeningJpaRepository.getScreeningBySurveyIdGreaterThanEqualAndSurveyIdIsIn(surveyIdList.getFirst(), surveyIdList);
    }

    @Override
    public Screening save(Screening screening) {
        return screeningJpaRepository.save(screening);
    }
}
