package OneQ.OnSurvey.domain.survey.repository;

import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.global.util.QuerydslUtils;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import static OneQ.OnSurvey.domain.survey.entity.QSurvey.survey;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SurveyRepositoryImpl implements SurveyRepository {
    private final SurveyJpaRepository surveyJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Survey> getSurveyById(Long surveyId) {
        return surveyJpaRepository.getSurveyById(surveyId);
    }

    @Override
    public List<Survey> getSurveyListByMemberId(Long memberId) {
        return surveyJpaRepository.getSurveysByMemberId(memberId);
    }

    @Override
    public Slice<Survey> getSurveyListByStatus(SurveyStatus status, Long lastSurveyId, Pageable pageable) {
        List<Long> surveyIds = jpaQueryFactory
            .select(survey.id)
            .from(survey)
            .where(
                survey.status.eq(status),
                survey.id.gt(lastSurveyId)
            )
            .orderBy(QuerydslUtils.getSort(pageable, survey))
            .limit(pageable.getPageSize() + 1)
            .fetch();

        if (surveyIds.isEmpty()) {
            return createSlice(List.of(), pageable);
        }

        List<Survey> surveyList = jpaQueryFactory
            .selectFrom(survey)
            .leftJoin(survey.interests).fetchJoin()
            .where(
                survey.id.in(surveyIds)
            )
            .orderBy(QuerydslUtils.getSort(pageable, survey))
            .limit(pageable.getPageSize() + 1)
            .fetch();

        return createSlice(surveyList, pageable);
    }

    @Override
    public Slice<Survey> getSurveyList(Long lastSurveyId, Pageable pageable) {
        List<Survey> surveyList = jpaQueryFactory.selectFrom(survey)
            .where(
                survey.id.gt(lastSurveyId)
            )
            .orderBy(QuerydslUtils.getSort(pageable, survey))
            .limit(pageable.getPageSize() + 1)
            .fetch();

        return createSlice(surveyList, pageable);
    }

    @Override
    public Survey save(Survey survey) {
        return surveyJpaRepository.save(survey);
    }

    @Override
    public void deleteById(Long surveyId) {
        surveyJpaRepository.deleteById(surveyId);
    }

    private Slice<Survey> createSlice(List<Survey> surveyList, Pageable pageable) {
        boolean hasNext = false;
        int size = pageable.getPageSize();

        if (surveyList.size() > size) {
            hasNext = true;
            surveyList.remove(size);
        }

        return new SliceImpl<>(surveyList, pageable, hasNext);
    }
}
