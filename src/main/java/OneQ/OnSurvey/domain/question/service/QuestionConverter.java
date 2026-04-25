package OneQ.OnSurvey.domain.question.service;

import OneQ.OnSurvey.domain.question.entity.Question;
import OneQ.OnSurvey.domain.question.entity.question.Choice;
import OneQ.OnSurvey.domain.question.entity.question.DateAnswer;
import OneQ.OnSurvey.domain.question.entity.question.Grid;
import OneQ.OnSurvey.domain.question.entity.question.Rating;
import OneQ.OnSurvey.domain.question.entity.question.Time;
import OneQ.OnSurvey.domain.question.model.QuestionType;
import OneQ.OnSurvey.domain.question.model.dto.GridOptionDto;
import OneQ.OnSurvey.domain.question.model.dto.OptionDto;
import OneQ.OnSurvey.domain.question.model.dto.QuestionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.type.ChoiceDto;
import OneQ.OnSurvey.domain.question.model.dto.type.DateDto;
import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;
import OneQ.OnSurvey.domain.question.model.dto.type.GridDto;
import OneQ.OnSurvey.domain.question.model.dto.type.RatingDto;
import OneQ.OnSurvey.domain.question.model.dto.type.TimeDto;

import java.util.List;

public class QuestionConverter {
    public static QuestionUpsertDto toQuestionUpsertDto(Long surveyId, List<DefaultQuestionDto> questions) {

        List<QuestionUpsertDto.UpsertInfo> upsertInfoList = questions.stream()
            .map(QuestionConverter::toUpsertInfo) // 각 DTO를 UpsertInfo로 변환
            .toList();

        return QuestionUpsertDto.builder()
            .surveyId(surveyId)
            .upsertInfoList(upsertInfoList)
            .build();
    }

    private static QuestionUpsertDto.UpsertInfo toUpsertInfo(DefaultQuestionDto dto) {
        if (dto == null) {
            return null;
        }

        // 1. 공통 필드 매핑
        QuestionUpsertDto.UpsertInfo.UpsertInfoBuilder builder = QuestionUpsertDto.UpsertInfo.builder()
            .questionId(dto.getQuestionId())
            .title(dto.getTitle())
            .description(dto.getDescription())
            .isRequired(dto.getIsRequired())
            .questionOrder(dto.getQuestionOrder())
            .questionType(QuestionType.valueOf(dto.getQuestionType()))
            .section(dto.getSection() != null ? dto.getSection() : 1) // section이 null인 경우 기본값 1 설정
            .imageUrl(dto.getImageUrl());

        // 2. 타입별 특정 필드 매핑
        switch (dto) {
            case ChoiceDto choiceDto -> builder.maxChoice(choiceDto.getMaxChoice())
                .hasNoneOption(choiceDto.getHasNoneOption() != null ? choiceDto.getHasNoneOption() : false)
                .hasCustomInput(choiceDto.getHasCustomInput() != null ? choiceDto.getHasCustomInput() : false)
                .isSectionDecidable(choiceDto.getIsSectionDecidable() != null ? choiceDto.getIsSectionDecidable() : false)
                .options(choiceDto.getOptions().stream().map(option ->
                    OptionDto.builder()
                        .optionId(option.getOptionId())
                        .content(option.getContent())
                        .nextSection(option.getNextSection())
                        .imageUrl(option.getImageUrl()).build()
                    ).toList()
                );
            case RatingDto ratingDto -> builder.minValue(ratingDto.getMinValue())
                .maxValue(ratingDto.getMaxValue())
                .rate(ratingDto.getRate());
            case DateDto dateDto -> builder.defaultDate(dateDto.getDate());
            case GridDto gridDto -> builder.isCheckbox(gridDto.getIsCheckbox())
                .isChoiceMixed(gridDto.getIsChoiceMixed() != null ? gridDto.getIsChoiceMixed() : false)
                .isChoiceDistinct(gridDto.getIsChoiceDistinct() != null ? gridDto.getIsChoiceDistinct() : false)
                .gridOptions(gridDto.getGridOptions().stream().map(option ->
                    GridOptionDto.builder()
                        .gridOptionId(option.getGridOptionId())
                        .isRow(option.getIsRow())
                        .content(option.getContent())
                        .order(option.getOrder()).build()
                    ).toList()
                );
            case TimeDto timeDto -> builder.isInterval(timeDto.getIsInterval() != null ? timeDto.getIsInterval() : false);
            default -> {
            }
        }

        return builder.build();
    }

    public static DefaultQuestionDto toQuestionDto(Question question) {
        return switch (question) {
            case Choice choice -> ChoiceDto.fromEntity(choice);
            case Rating rating -> RatingDto.fromEntity(rating);
            case DateAnswer dateAnswer -> DateDto.fromEntity(dateAnswer);
            case Grid grid -> GridDto.fromEntity(grid);
            case Time time -> TimeDto.fromEntity(time);
            default -> DefaultQuestionDto.fromEntity(question);
        };
    }
}
