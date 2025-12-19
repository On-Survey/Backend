package OneQ.OnSurvey.domain.survey.repository.surveyInfo;

import OneQ.OnSurvey.domain.survey.entity.SurveyInfo;
import OneQ.OnSurvey.domain.survey.model.dto.SurveySegmentation;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.set;
import static OneQ.OnSurvey.domain.survey.entity.QSurveyInfo.surveyInfo;
import static OneQ.OnSurvey.domain.survey.entity.QSurvey.survey;

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

    @Override
    public SurveySegmentation findSegmentationBySurveyId(Long surveyId) {
        Long infoId = queryFactory
            .select(surveyInfo.infoId)
            .from(surveyInfo)
            .where(surveyInfo.surveyId.eq(surveyId))
            .fetchOne();

        return queryFactory
            .from(surveyInfo)
            .leftJoin(surveyInfo.ages)
            .where(surveyInfo.infoId.eq(infoId))
            .transform(groupBy(surveyInfo.surveyId).as(
                Projections.fields(
                    SurveySegmentation.class,
                    surveyInfo.surveyId,
                    surveyInfo.gender,
                    set(surveyInfo.ages).as("ages"),
                    surveyInfo.residence
                )
            ))
            .get(surveyId);
    }
}
