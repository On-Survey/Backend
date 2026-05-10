package OneQ.OnSurvey.domain.question.repository.gridOption;

import OneQ.OnSurvey.domain.question.entity.GridOption;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

import static OneQ.OnSurvey.domain.question.entity.QQuestion.question;
import static OneQ.OnSurvey.domain.question.entity.QGridOption.gridOption;

@Repository
@RequiredArgsConstructor
public class GridOptionRepositoryImpl implements GridOptionRepository {

    private final GridOptionJpaRepository gridOptionJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<GridOption> getGridOptionsByQuestionIds(Collection<Long> questionIds) {
        return gridOptionJpaRepository.getGridOptionsByQuestionIdIsInOrderByGridOptionIdAsc(questionIds);
    }

    @Override
    public List<GridOption> getGridOptionsByQuestionId(Long questionId) {
        return gridOptionJpaRepository.getGridOptionsByQuestionId(questionId);
    }

    @Override
    public List<GridOption> saveAll(Collection<GridOption> gridOptions) {
        return gridOptionJpaRepository.saveAllAndFlush(gridOptions);
    }

    @Override
    public void deleteAll(Collection<Long> ids) {
        gridOptionJpaRepository.deleteAllByIdInBatch(ids);
    }

    @Override
    public void deleteBySections(Long surveyId, Collection<Integer> sections) {
        jpaQueryFactory.delete(
            gridOption
        ).where(
            gridOption.questionId.in(
                JPAExpressions
                    .select(question.questionId)
                    .from(question)
                    .where(
                        question.surveyId.eq(surveyId),
                        question.section.notIn(sections)
                    )
            )
        ).execute();
    }
}
