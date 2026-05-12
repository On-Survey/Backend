package OneQ.OnSurvey.domain.question.service;

import OneQ.OnSurvey.domain.question.entity.ChoiceOption;
import OneQ.OnSurvey.domain.question.entity.GridOption;
import OneQ.OnSurvey.domain.question.model.dto.GridOptionDto;
import OneQ.OnSurvey.domain.question.model.dto.OptionDto;
import OneQ.OnSurvey.domain.question.repository.choiceOption.ChoiceOptionRepository;
import OneQ.OnSurvey.domain.question.repository.gridOption.GridOptionRepository;
import OneQ.OnSurvey.domain.question.repository.question.QuestionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class QuestionQueryServiceTest {

    @Mock private QuestionRepository questionRepository;
    @Mock private ChoiceOptionRepository choiceOptionRepository;
    @Mock private GridOptionRepository gridOptionRepository;

    @InjectMocks
    private QuestionQueryService questionQueryService;

    @Test
    @DisplayName("getOptionsByQuestionIdList - 보기를 OptionDto로 변환하여 반환")
    void getOptionsByQuestionIdList_returnsOptionDtos() {
        ChoiceOption option = ChoiceOption.of(1L, "선택지1", null, null);
        ReflectionTestUtils.setField(option, "choiceOptionId", 10L);
        given(choiceOptionRepository.getOptionsByQuestionIds(List.of(1L))).willReturn(List.of(option));

        List<OptionDto> result = questionQueryService.getOptionsByQuestionIdList(List.of(1L));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOptionId()).isEqualTo(10L);
        assertThat(result.get(0).getContent()).isEqualTo("선택지1");
        assertThat(result.get(0).getQuestionId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getOptionsByQuestionIdList - 보기 없으면 빈 리스트 반환")
    void getOptionsByQuestionIdList_empty_returnsEmpty() {
        given(choiceOptionRepository.getOptionsByQuestionIds(List.of(1L))).willReturn(List.of());

        List<OptionDto> result = questionQueryService.getOptionsByQuestionIdList(List.of(1L));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getGridOptionsByQuestionIdList - 그리드 옵션을 GridOptionDto로 변환하여 반환")
    void getGridOptionsByQuestionIdList_returnsGridOptionDtos() {
        GridOption gridOption = GridOption.of(2L, true, "행1", 0);
        ReflectionTestUtils.setField(gridOption, "gridOptionId", 20L);
        given(gridOptionRepository.getGridOptionsByQuestionIds(List.of(2L))).willReturn(List.of(gridOption));

        List<GridOptionDto> result = questionQueryService.getGridOptionsByQuestionIdList(List.of(2L));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGridOptionId()).isEqualTo(20L);
        assertThat(result.get(0).getIsRow()).isTrue();
        assertThat(result.get(0).getContent()).isEqualTo("행1");
    }

    @Test
    @DisplayName("countQuestionsBySurveyId - repository에 위임")
    void countQuestionsBySurveyId_delegatesToRepository() {
        given(questionRepository.countBySurveyId(1L)).willReturn(7);

        int result = questionQueryService.countQuestionsBySurveyId(1L);

        assertThat(result).isEqualTo(7);
    }
}
