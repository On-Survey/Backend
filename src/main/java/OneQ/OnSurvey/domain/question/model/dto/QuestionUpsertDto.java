package OneQ.OnSurvey.domain.question.model.dto;

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
        Integer order;
        Integer maxChoice;
        Boolean hasNoneOption;
        Boolean hasCustomInput;
        String minValue;
        String maxValue;
        TextType textType;
        LocalDateTime defaultDate;
    }
}