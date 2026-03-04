package OneQ.OnSurvey.domain.admin.domain.model.survey;

import lombok.Builder;

import java.time.LocalDate;
import java.util.Set;

@Builder
public record SurveyQuestion(
    Long questionId,
    String questionType,
    String title,
    String description,
    Boolean isRequired,
    Integer questionOrder,
    Integer section,
    String imageUrl,

    ChoiceProp choiceProperty,
    RatingProp ratingProperty,
    DateProp dateProperty
) {
    public record ChoiceProp (
        Integer maxChoice,
        Boolean hasCustomInput,
        Boolean hasNoneOption,
        Boolean isSectionDecidable,

        Set<Option> options
    ) {
        public record Option (
            String content,
            Integer nextSection,
            String imageUrl
        ) {}
    }

    public record RatingProp (
        String minValue,
        String maxValue,
        Integer rate
    ) {}

    public record DateProp (
        LocalDate defaultDate
    ) {}
}
