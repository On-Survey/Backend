package OneQ.OnSurvey.domain.question.repository.section;

import OneQ.OnSurvey.domain.question.entity.Section;
import OneQ.OnSurvey.domain.question.model.dto.SectionDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

import static OneQ.OnSurvey.domain.question.entity.QSection.section;

@Repository
@RequiredArgsConstructor
public class SectionRepositoryImpl implements SectionRepository{

    private final SectionJpaRepository sectionJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Section findBySurveyIdAndOrder(long surveyId, int order) {
        return sectionJpaRepository.findBySurveyIdAndSectionOrder(surveyId, order);
    }

    @Override
    public List<Section> findAllSectionBySurveyId(long surveyId) {
        return sectionJpaRepository.findAllBySurveyId(surveyId);
    }

    @Override
    public SectionDto findSectionDtoBySurveyIdAndOrder(long surveyId, int order) {
        return jpaQueryFactory.select(Projections.constructor(SectionDto.class,
            section.sectionId,
            section.title,
            section.description,
            section.sectionOrder,
            section.nextSection
        ))
            .from(section)
            .where(
                section.surveyId.eq(surveyId),
                section.sectionOrder.eq(order)
            )
            .fetchOne();
    }

    @Override
    public List<SectionDto> findAllSectionDtoBySurveyId(long surveyId) {
        return jpaQueryFactory.select(Projections.constructor(SectionDto.class,
            section.sectionId,
            section.title,
            section.description,
            section.sectionOrder,
            section.nextSection
        ))
            .from(section)
            .where(
                section.surveyId.eq(surveyId)
            )
            .orderBy(section.sectionOrder.asc())
            .fetch();
    }

    @Override
    public Section save(Section section) {
        return sectionJpaRepository.save(section);
    }

    @Override
    public List<Section> saveAll(Collection<Section> sections) {
        return sectionJpaRepository.saveAll(sections);
    }

    @Override
    public void delete(Section section) {
        sectionJpaRepository.delete(section);
    }

    @Override
    public void deleteAll(Collection<Long> sectionIds) {
        sectionJpaRepository.deleteAllByIdInBatch(sectionIds);
    }
}
