package OneQ.OnSurvey.domain.survey.repository;

import OneQ.OnSurvey.domain.survey.entity.SurveyInfo;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static OneQ.OnSurvey.domain.survey.entity.QSurveyInfo.surveyInfo;

@Repository
@RequiredArgsConstructor
public class SurveyInfoRepositoryImpl implements SurveyInfoRepository {

    private final SurveyInfoJpaRepository surveyInfoJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public SurveyInfo save(SurveyInfo surveyInfo) {
        surveyInfoJpaRepository.save(surveyInfo);
        return surveyInfo;
    }

    @Override
    public Optional<SurveyInfo> findBySurveyId(Long surveyId) {
        SurveyInfo result = queryFactory
                .selectFrom(surveyInfo)
                .leftJoin(surveyInfo.ages).fetchJoin()
                .where(surveyInfo.surveyId.eq(surveyId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<SurveyInfo> findBySurveyIdIn(List<Long> surveyIds) {
        return surveyInfoJpaRepository.findBySurveyIdIn(surveyIds);
    }
}
