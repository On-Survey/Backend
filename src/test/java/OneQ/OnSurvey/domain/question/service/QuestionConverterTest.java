package OneQ.OnSurvey.domain.question.service;

import OneQ.OnSurvey.domain.question.entity.question.*;
import OneQ.OnSurvey.domain.question.model.QuestionType;
import OneQ.OnSurvey.domain.question.model.dto.GridOptionDto;
import OneQ.OnSurvey.domain.question.model.dto.OptionDto;
import OneQ.OnSurvey.domain.question.model.dto.QuestionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.type.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionConverterTest {

    @Test
    @DisplayName("Choice 엔티티 → ChoiceDto 변환")
    void toQuestionDto_choice() {
        Choice choice = Choice.of(
                1L, 1, "Q1", "설명", true, 1,
                3, false, true, false,
                QuestionType.CHOICE, null
        );

        DefaultQuestionDto dto = QuestionConverter.toQuestionDto(choice);

        assertThat(dto).isInstanceOf(ChoiceDto.class);
        ChoiceDto choiceDto = (ChoiceDto) dto;
        assertThat(choiceDto.getMaxChoice()).isEqualTo(3);
        assertThat(choiceDto.getHasCustomInput()).isTrue();
        assertThat(choiceDto.getHasNoneOption()).isFalse();
        assertThat(choiceDto.getIsSectionDecidable()).isFalse();
        assertThat(choiceDto.getQuestionType()).isEqualTo(QuestionType.Values.CHOICE);
        assertThat(choiceDto.getTitle()).isEqualTo("Q1");
    }

    @Test
    @DisplayName("Rating 엔티티 → RatingDto 변환")
    void toQuestionDto_rating() {
        Rating rating = Rating.of(
                1L, 2, "평가", "설명", false, 1,
                "매우 좋음", "매우 나쁨", 5, QuestionType.RATING, null
        );

        DefaultQuestionDto dto = QuestionConverter.toQuestionDto(rating);

        assertThat(dto).isInstanceOf(RatingDto.class);
        RatingDto ratingDto = (RatingDto) dto;
        assertThat(ratingDto.getRate()).isEqualTo(5);
        assertThat(ratingDto.getMaxValue()).isEqualTo("매우 좋음");
        assertThat(ratingDto.getMinValue()).isEqualTo("매우 나쁨");
        assertThat(ratingDto.getQuestionType()).isEqualTo(QuestionType.Values.RATING);
    }

    @Test
    @DisplayName("DateAnswer 엔티티 → DateDto 변환")
    void toQuestionDto_date() {
        LocalDateTime now = LocalDateTime.of(2024, 1, 15, 12, 0);
        DateAnswer dateAnswer = DateAnswer.of(
                1L, 3, "날짜 질문", null, false, 1,
                now, QuestionType.DATE, null
        );

        DefaultQuestionDto dto = QuestionConverter.toQuestionDto(dateAnswer);

        assertThat(dto).isInstanceOf(DateDto.class);
        DateDto dateDto = (DateDto) dto;
        assertThat(dateDto.getDate()).isEqualTo(now);
        assertThat(dateDto.getQuestionType()).isEqualTo(QuestionType.Values.DATE);
    }

    @Test
    @DisplayName("Grid 엔티티 → GridDto 변환")
    void toQuestionDto_grid() {
        Grid grid = Grid.of(
                1L, 4, "그리드 질문", null, true, 1,
                QuestionType.GRID, null, true, false, true
        );

        DefaultQuestionDto dto = QuestionConverter.toQuestionDto(grid);

        assertThat(dto).isInstanceOf(GridDto.class);
        GridDto gridDto = (GridDto) dto;
        assertThat(gridDto.getIsCheckbox()).isTrue();
        assertThat(gridDto.getIsChoiceMixed()).isFalse();
        assertThat(gridDto.getIsChoiceDistinct()).isTrue();
        assertThat(gridDto.getQuestionType()).isEqualTo(QuestionType.Values.GRID);
    }

    @Test
    @DisplayName("Time 엔티티 → TimeDto 변환")
    void toQuestionDto_time() {
        Time time = Time.of(
                1L, 5, "시간 질문", null, false, 1,
                QuestionType.TIME, null, true
        );

        DefaultQuestionDto dto = QuestionConverter.toQuestionDto(time);

        assertThat(dto).isInstanceOf(TimeDto.class);
        TimeDto timeDto = (TimeDto) dto;
        assertThat(timeDto.getIsInterval()).isTrue();
        assertThat(timeDto.getQuestionType()).isEqualTo(QuestionType.Values.TIME);
    }

    @Test
    @DisplayName("기본 질문 엔티티 → DefaultQuestionDto 변환")
    void toQuestionDto_default() {
        ShortAnswer shortAnswer = ShortAnswer.of(
                1L, 6, "단답형", null, false, 1, QuestionType.SHORT, null
        );

        DefaultQuestionDto dto = QuestionConverter.toQuestionDto(shortAnswer);

        assertThat(dto.getClass()).isEqualTo(DefaultQuestionDto.class);
        assertThat(dto.getQuestionType()).isEqualTo(QuestionType.Values.SHORT);
        assertThat(dto.getTitle()).isEqualTo("단답형");
    }

    @Test
    @DisplayName("ChoiceDto 리스트 → QuestionUpsertDto 변환 (기본 필드)")
    void toQuestionUpsertDto_withChoiceDto() {
        OptionDto option = OptionDto.builder()
                .optionId(10L).content("선택지1").nextSection(null).imageUrl(null).build();

        ChoiceDto choiceDto = ChoiceDto.builder()
                .questionId(1L)
                .questionType(QuestionType.Values.CHOICE)
                .title("객관식 질문")
                .description("설명")
                .isRequired(true)
                .questionOrder(1)
                .section(1)
                .maxChoice(2)
                .hasNoneOption(false)
                .hasCustomInput(true)
                .isSectionDecidable(false)
                .options(List.of(option))
                .build();

        QuestionUpsertDto result = QuestionConverter.toQuestionUpsertDto(99L, List.of(choiceDto));

        assertThat(result.getSurveyId()).isEqualTo(99L);
        assertThat(result.getUpsertInfoList()).hasSize(1);

        QuestionUpsertDto.UpsertInfo info = result.getUpsertInfoList().get(0);
        assertThat(info.getQuestionId()).isEqualTo(1L);
        assertThat(info.getQuestionType()).isEqualTo(QuestionType.CHOICE);
        assertThat(info.getMaxChoice()).isEqualTo(2);
        assertThat(info.getHasCustomInput()).isTrue();
        assertThat(info.getOptions()).hasSize(1);
        assertThat(info.getOptions().get(0).getContent()).isEqualTo("선택지1");
    }

    @Test
    @DisplayName("RatingDto → UpsertInfo 변환")
    void toQuestionUpsertDto_withRatingDto() {
        RatingDto ratingDto = RatingDto.builder()
                .questionId(2L)
                .questionType(QuestionType.Values.RATING)
                .title("평가 질문")
                .isRequired(false)
                .questionOrder(2)
                .section(1)
                .minValue("나쁨")
                .maxValue("좋음")
                .rate(10)
                .build();

        QuestionUpsertDto result = QuestionConverter.toQuestionUpsertDto(1L, List.of(ratingDto));

        QuestionUpsertDto.UpsertInfo info = result.getUpsertInfoList().get(0);
        assertThat(info.getQuestionType()).isEqualTo(QuestionType.RATING);
        assertThat(info.getRate()).isEqualTo(10);
        assertThat(info.getMinValue()).isEqualTo("나쁨");
        assertThat(info.getMaxValue()).isEqualTo("좋음");
    }

    @Test
    @DisplayName("GridDto → UpsertInfo 변환")
    void toQuestionUpsertDto_withGridDto() {
        GridOptionDto gridOption = GridOptionDto.builder()
                .gridOptionId(1L).isRow(true).content("행1").order(1).build();

        GridDto gridDto = GridDto.builder()
                .questionId(3L)
                .questionType(QuestionType.Values.GRID)
                .title("그리드 질문")
                .isRequired(true)
                .questionOrder(3)
                .section(2)
                .isCheckbox(true)
                .isChoiceMixed(null)
                .isChoiceDistinct(null)
                .gridOptions(List.of(gridOption))
                .build();

        QuestionUpsertDto result = QuestionConverter.toQuestionUpsertDto(1L, List.of(gridDto));

        QuestionUpsertDto.UpsertInfo info = result.getUpsertInfoList().get(0);
        assertThat(info.getIsCheckbox()).isTrue();
        assertThat(info.getIsChoiceMixed()).isFalse();
        assertThat(info.getIsChoiceDistinct()).isFalse();
        assertThat(info.getGridOptions()).hasSize(1);
    }

    @Test
    @DisplayName("TimeDto → UpsertInfo 변환 (isInterval null이면 false)")
    void toQuestionUpsertDto_withTimeDto_nullIsInterval() {
        TimeDto timeDto = TimeDto.builder()
                .questionId(4L)
                .questionType(QuestionType.Values.TIME)
                .title("시간 질문")
                .isRequired(false)
                .questionOrder(4)
                .section(1)
                .isInterval(null)
                .build();

        QuestionUpsertDto result = QuestionConverter.toQuestionUpsertDto(1L, List.of(timeDto));

        QuestionUpsertDto.UpsertInfo info = result.getUpsertInfoList().get(0);
        assertThat(info.getIsInterval()).isFalse();
    }

    @Test
    @DisplayName("section이 null이면 기본값 1로 설정")
    void toQuestionUpsertDto_sectionNullDefaultsToOne() {
        DefaultQuestionDto dto = DefaultQuestionDto.builder()
                .questionId(5L)
                .questionType(QuestionType.Values.SHORT)
                .title("단답형")
                .isRequired(false)
                .questionOrder(1)
                .section(null)
                .build();

        QuestionUpsertDto result = QuestionConverter.toQuestionUpsertDto(1L, List.of(dto));

        assertThat(result.getUpsertInfoList().get(0).getSection()).isEqualTo(1);
    }

    @Test
    @DisplayName("빈 리스트 입력 시 upsertInfoList도 빈 리스트")
    void toQuestionUpsertDto_emptyList() {
        QuestionUpsertDto result = QuestionConverter.toQuestionUpsertDto(1L, List.of());

        assertThat(result.getUpsertInfoList()).isEmpty();
    }
}
