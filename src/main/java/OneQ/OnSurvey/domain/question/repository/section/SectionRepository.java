package OneQ.OnSurvey.domain.question.repository.section;

import OneQ.OnSurvey.domain.question.entity.Section;
import OneQ.OnSurvey.domain.question.model.dto.SectionDto;

import java.util.Collection;
import java.util.List;

public interface SectionRepository {
    Section findBySurveyIdAndOrder(long surveyId, int order);
    List<Section> findAllSectionBySurveyId(long surveyId);
    SectionDto findSectionDtoBySurveyIdAndOrder(long surveyId, int order);
    List<SectionDto> findAllSectionDtoBySurveyId(long surveyId);
    Section save(Section section);
    List<Section> saveAll(Collection<Section> sections);
    void delete(Section section);
    void deleteAll(Collection<Long> sectionIds);
}
