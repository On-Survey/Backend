package OneQ.OnSurvey.domain.question.model.dto.type;

import OneQ.OnSurvey.domain.question.entity.question.Rating;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter @SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RatingDto extends DefaultQuestionDto {
    private String minValue;
    private String maxValue;

    public static RatingDto fromEntity(Rating rating) {
        return RatingDto.builder()
            .minValue(rating.getMinValue())
            .maxValue(rating.getMaxValue())
            .questionId(rating.getQuestionId())
            .surveyId(rating.getSurveyId())
            .questionType(rating.getType())
            .title(rating.getTitle())
            .description(rating.getDescription())
            .isRequired(rating.getIsRequired())
            .questionOrder(rating.getOrder())
            .build();
    }
}
