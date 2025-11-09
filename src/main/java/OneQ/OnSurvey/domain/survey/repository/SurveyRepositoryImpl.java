package OneQ.OnSurvey.domain.survey.repository;

import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.global.util.QuerydslUtils;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import static OneQ.OnSurvey.domain.survey.entity.QSurvey.survey;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SurveyRepositoryImpl implements SurveyRepository {
    private final SurveyJpaRepository surveyJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Survey getSurveyById(Long surveyId) {
        return surveyJpaRepository.getSurveyById(surveyId);
    }

    @Override
    public List<Survey> getSurveyListByMemberId(Long memberId) {
        return surveyJpaRepository.getSurveysByMemberId(memberId);
    }

    @Override
    public List<Survey> getSurveyList(SurveyStatus status, Long lastSurveyId, Pageable pageable) {
        return jpaQueryFactory.selectFrom(survey)
            .where(
                survey.status.eq(status),
                survey.id.gt(lastSurveyId)
            )
            .orderBy(QuerydslUtils.getSort(pageable, survey))
            .limit(pageable.getPageSize())
            .fetch();
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
