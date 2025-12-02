package OneQ.OnSurvey.domain.survey.repository.screening;

import OneQ.OnSurvey.domain.survey.entity.Screening;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static OneQ.OnSurvey.domain.survey.entity.QScreening.screening;

@Repository
@RequiredArgsConstructor
public class ScreeningRepositoryImpl implements ScreeningRepository {

    private final ScreeningJpaRepository screeningJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Screening getScreeningBySurveyId(Long surveyId) {
        return screeningJpaRepository.getScreeningBySurveyId(surveyId);
    }

    @Override
    public List<Screening> getScreeningListBySurveyIdList(List<Long> surveyIdList) {
        return screeningJpaRepository.getScreeningBySurveyIdGreaterThanEqualAndSurveyIdIsIn(surveyIdList.getFirst(), surveyIdList);
    }

    @Override
    public Boolean getScreeningAnswer(Long screeningId) {
        return jpaQueryFactory.select(screening.answer)
            .from(screening)
            .where(screening.id.eq(screeningId))
            .fetchOne();
    }

    @Override
    public Long getSurveyId(Long screeningId) {
        return jpaQueryFactory.select(screening.surveyId)
            .from(screening)
            .where(screening.id.eq(screeningId))
            .fetchOne();
    }

    @Override
    public Screening save(Screening screening) {
        return screeningJpaRepository.save(screening);
    }
}
