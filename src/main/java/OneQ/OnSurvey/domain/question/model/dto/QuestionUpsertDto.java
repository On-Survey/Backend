package OneQ.OnSurvey.domain.question.model.dto;

import OneQ.OnSurvey.domain.question.entity.Question;
import OneQ.OnSurvey.domain.question.entity.question.Choice;
import OneQ.OnSurvey.domain.question.entity.question.DateAnswer;
import OneQ.OnSurvey.domain.question.entity.question.Rating;
import OneQ.OnSurvey.domain.question.model.QuestionType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Builder
public class QuestionUpsertDto {
    private final Long surveyId;
    private final List<UpsertInfo> upsertInfoList;

    @Getter @Builder @ToString
    public static class UpsertInfo {
        Long questionId;
        String title;
        String description;
        Boolean isRequired;
        QuestionType questionType;
        Integer questionOrder;
        Integer section;

        // Choice 필드
        Integer maxChoice;
        Boolean hasNoneOption;
        Boolean hasCustomInput;
        Boolean isSectionDecidable;
        @Setter
        List<OptionDto> options;

        // Rating 필드
        String minValue;
        String maxValue;
        Integer rate; // 선택 가능한 점수 개수

        // Date 필드
        LocalDateTime defaultDate;
    }

    public static UpsertInfo fromEntity(Question question) {
        UpsertInfo.UpsertInfoBuilder builder = UpsertInfo.builder()
            .questionId(question.getQuestionId())
            .title(question.getTitle())
            .description(question.getDescription())
            .isRequired(question.getIsRequired())
            .questionOrder(question.getOrder())
            .section(question.getSection())
            .questionType(QuestionType.valueOf(question.getType()));

        return switch (QuestionType.valueOf(question.getType())) {
            case CHOICE -> {
                Choice choice = (Choice) question;
                yield builder
                    .maxChoice(choice.getMaxChoice())
                    .hasNoneOption(choice.getHasNoneOption())
                    .hasCustomInput(choice.getHasCustomInput())
                    .isSectionDecidable(choice.getIsSectionDecidable())
                    .build();
            }
            case RATING -> {
                Rating rating = (Rating) question;
                yield builder
                    .maxValue(rating.getMaxValue())
                    .minValue(rating.getMinValue())
                    .rate(rating.getRate())
                    .build();
            }
            case DATE -> {
                DateAnswer dateAnswer = (DateAnswer) question;
                yield builder.defaultDate(dateAnswer.getDefaultDate()).build();
            }
            default -> builder.build();
        };
    }
}