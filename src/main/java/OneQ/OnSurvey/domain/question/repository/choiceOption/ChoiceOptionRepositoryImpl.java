package OneQ.OnSurvey.domain.question.repository.choiceOption;

import OneQ.OnSurvey.domain.question.entity.ChoiceOption;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

import static OneQ.OnSurvey.domain.question.entity.QQuestion.question;
import static OneQ.OnSurvey.domain.question.entity.QChoiceOption.choiceOption;

@Repository
@RequiredArgsConstructor
public class ChoiceOptionRepositoryImpl implements ChoiceOptionRepository {

    private final ChoiceOptionJpaRepository choiceOptionJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ChoiceOption> getOptionsByQuestionIds(Collection<Long> questionIdList) {
        return choiceOptionJpaRepository.getChoiceOptionsByQuestionIdIsInOrderByChoiceOptionIdAsc(questionIdList);
    }

    @Override
    public List<ChoiceOption> getOptionsByQuestionId(Long questionId) {
        return choiceOptionJpaRepository.getChoiceOptionsByQuestionId(questionId);
    }

    @Override
    public List<ChoiceOption> saveAll(Collection<ChoiceOption> choiceOptions) {
        return choiceOptionJpaRepository.saveAllAndFlush(choiceOptions);
    }

    @Override
    public void deleteAll(Collection<Long> optionIdList) {
        choiceOptionJpaRepository.deleteAllByIdInBatch(optionIdList);
    }

    @Override
    public void deleteBySections(Long surveyId, Collection<Integer> sections) {
        jpaQueryFactory.delete(
            choiceOption
        ).where(
            choiceOption.questionId.in(
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
