package OneQ.OnSurvey.domain.question.service;

import OneQ.OnSurvey.domain.question.entity.Question;
import OneQ.OnSurvey.domain.question.entity.question.Choice;
import OneQ.OnSurvey.domain.question.entity.question.DateAnswer;
import OneQ.OnSurvey.domain.question.entity.question.Rating;
import OneQ.OnSurvey.domain.question.model.QuestionType;
import OneQ.OnSurvey.domain.question.model.dto.OptionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.QuestionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.type.ChoiceDto;
import OneQ.OnSurvey.domain.question.model.dto.type.DateDto;
import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;
import OneQ.OnSurvey.domain.question.model.dto.type.RatingDto;

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
            .questionType(QuestionType.valueOf(dto.getQuestionType()));

        // 2. 타입별 특정 필드 매핑
        switch (dto) {
            case ChoiceDto choiceDto -> builder.maxChoice(choiceDto.getMaxChoice())
                .hasNoneOption(choiceDto.getHasNoneOption())
                .hasCustomInput(choiceDto.getHasCustomInput())
                .options(choiceDto.getOptions().stream().map(option ->
                    OptionUpsertDto.OptionInfo.builder()
                        .optionId(option.getOptionId())
                        .content(option.getContent())
                        .nextQuestionId(option.getNextQuestionId()).build()
                    ).toList()
                );
            case RatingDto ratingDto -> builder.minValue(ratingDto.getMinValue())
                .maxValue(ratingDto.getMaxValue())
                .rate(ratingDto.getRate());
            case DateDto dateDto -> builder.defaultDate(dateDto.getDate());
            default -> {
            }
        }

        return builder.build();
    }

    public static DefaultQuestionDto toQuestionDto(Question question) {
        if (question instanceof Choice choice) {
            return ChoiceDto.fromEntity(choice);
        } else if (question instanceof Rating rating) {
            return RatingDto.fromEntity(rating);
        } else if (question instanceof DateAnswer dateAnswer) {
            return DateDto.fromEntity(dateAnswer);
        } else {
            return DefaultQuestionDto.fromEntity(question);
        }
    }
}
