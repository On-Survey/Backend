package OneQ.OnSurvey.domain.question.model.dto;

import OneQ.OnSurvey.domain.question.entity.Question;
import OneQ.OnSurvey.domain.question.entity.question.Choice;
import OneQ.OnSurvey.domain.question.entity.question.Rating;
import OneQ.OnSurvey.domain.question.entity.question.Text;
import OneQ.OnSurvey.domain.question.model.QuestionType;
import OneQ.OnSurvey.domain.question.model.TextType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Builder
public class QuestionUpsertDto {
    private final Long surveyId;
    private final List<UpsertInfo> upsertInfoList;

    @Getter @Builder
    public static class UpsertInfo {
        Long questionId;
        String title;
        String description;
        Boolean isRequired;
        QuestionType questionType;
        Integer questionOrder;

        // Choice 필드
        Integer maxChoice;
        Boolean hasNoneOption;
        Boolean hasCustomInput;
        List<OptionUpsertDto.OptionInfo> options;

        // Rating 필드
        String minValue;
        String maxValue;

        // Text 필드
        TextType textType;

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
            .questionType(question.getType());

        return switch (question.getType()) {
            case CHOICE -> {
                Choice choice = (Choice) question;
                yield builder
                    .maxChoice(choice.getMaxChoice())
                    .hasNoneOption(choice.getHasNoneOption())
                    .hasCustomInput(choice.getHasCustomInput())
                    .build();
            }
            case RATING -> {
                Rating rating = (Rating) question;
                yield builder
                    .maxValue(rating.getMaxValue())
                    .minValue(rating.getMinValue())
                    .build();
            }
            case DATE -> {
                Text text = (Text) question;
                yield builder.defaultDate(text.getDefaultDate()).build();
            }
            default -> builder.build();
        };
    }
}