package OneQ.OnSurvey.domain.question.service;

import OneQ.OnSurvey.domain.question.entity.Section;
import OneQ.OnSurvey.domain.question.model.dto.SectionDto;
import OneQ.OnSurvey.domain.question.repository.choiceOption.ChoiceOptionRepository;
import OneQ.OnSurvey.domain.question.repository.gridOption.GridOptionRepository;
import OneQ.OnSurvey.domain.question.repository.question.QuestionRepository;
import OneQ.OnSurvey.domain.question.repository.section.SectionRepository;
import OneQ.OnSurvey.domain.survey.SurveyErrorCode;
import OneQ.OnSurvey.global.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class QuestionCommandServiceTest {

    @Mock private ChoiceOptionRepository choiceOptionRepository;
    @Mock private GridOptionRepository gridOptionRepository;
    @Mock private SectionRepository sectionRepository;
    @Mock private QuestionRepository questionRepository;

    @InjectMocks
    private QuestionCommandService questionCommandService;

    private Section buildSection(Long sectionId, Long surveyId, String title, int order, int nextSection) {
        Section section = Section.builder()
                .surveyId(surveyId)
                .title(title)
                .sectionOrder(order)
                .nextSection(nextSection)
                .build();
        if (sectionId != null) {
            ReflectionTestUtils.setField(section, "sectionId", sectionId);
        }
        return section;
    }

    @Test
    @DisplayName("upsertSections - null 제목이면 SURVEY_FORM_INVALID_SECTION 예외")
    void upsertSections_nullTitle_throwsInvalidSection() {
        SectionDto invalid = new SectionDto(null, null, null, 1, 2);

        assertThatThrownBy(() -> questionCommandService.upsertSections(1L, List.of(invalid)))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(SurveyErrorCode.SURVEY_FORM_INVALID_SECTION));
    }

    @Test
    @DisplayName("upsertSections - 공백 제목이면 SURVEY_FORM_INVALID_SECTION 예외")
    void upsertSections_blankTitle_throwsInvalidSection() {
        SectionDto invalid = new SectionDto(null, "   ", null, 1, 2);

        assertThatThrownBy(() -> questionCommandService.upsertSections(1L, List.of(invalid)))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(SurveyErrorCode.SURVEY_FORM_INVALID_SECTION));
    }

    @Test
    @DisplayName("upsertSections - null order이면 SURVEY_FORM_INVALID_SECTION 예외")
    void upsertSections_nullOrder_throwsInvalidSection() {
        SectionDto invalid = new SectionDto(null, "제목", null, null, 2);

        assertThatThrownBy(() -> questionCommandService.upsertSections(1L, List.of(invalid)))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(SurveyErrorCode.SURVEY_FORM_INVALID_SECTION));
    }

    @Test
    @DisplayName("upsertSections - 신규 섹션이면 새 Section 엔티티 생성 및 저장")
    void upsertSections_newSection_createsAndSaves() {
        SectionDto newDto = new SectionDto(null, "새 섹션", "설명", 1, 2);
        Section savedSection = buildSection(100L, 1L, "새 섹션", 1, 2);
        given(sectionRepository.findAllSectionBySurveyId(1L)).willReturn(List.of());
        given(sectionRepository.saveAll(any())).willReturn(List.of(savedSection));

        List<SectionDto> result = questionCommandService.upsertSections(1L, List.of(newDto));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).sectionId()).isEqualTo(100L);
        assertThat(result.get(0).title()).isEqualTo("새 섹션");
        verify(sectionRepository).saveAll(any());
    }

    @Test
    @DisplayName("upsertSections - 기존 섹션이면 내용 업데이트")
    void upsertSections_existingSection_updatesSection() {
        Section existing = buildSection(50L, 1L, "기존 섹션", 1, 2);
        SectionDto updateDto = new SectionDto(50L, "수정된 섹션", "수정 설명", 1, 3);
        given(sectionRepository.findAllSectionBySurveyId(1L)).willReturn(List.of(existing));
        given(sectionRepository.saveAll(any())).willReturn(List.of(existing));

        questionCommandService.upsertSections(1L, List.of(updateDto));

        assertThat(existing.getTitle()).isEqualTo("수정된 섹션");
        assertThat(existing.getNextSection()).isEqualTo(3);
        verify(sectionRepository).saveAll(any());
    }

    @Test
    @DisplayName("upsertSections - DB에만 있는 섹션은 삭제")
    void upsertSections_extraDbSections_deletesOldSections() {
        Section extra = buildSection(50L, 1L, "삭제될 섹션", 1, 2);
        given(sectionRepository.findAllSectionBySurveyId(1L)).willReturn(List.of(extra));

        questionCommandService.upsertSections(1L, List.of());

        verify(sectionRepository).deleteAll(List.of(50L));
    }
}
