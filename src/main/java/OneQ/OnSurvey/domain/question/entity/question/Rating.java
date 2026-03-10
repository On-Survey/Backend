package OneQ.OnSurvey.domain.question.entity.question;

import OneQ.OnSurvey.domain.question.entity.Question;
import OneQ.OnSurvey.domain.question.model.QuestionType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter @SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@DiscriminatorValue(value = QuestionType.Values.RATING)
public class Rating extends Question {

    @Column(name = "max_value")
    private String maxValue;

    @Column(name = "min_value")
    private String minValue;

    @Column(name = "rate")
    private Integer rate;

    public static Rating of(
        Long surveyId,
        Integer order,
        String title,
        String description,
        Boolean isRequired,
        Integer section,
        String maxValue,
        String minValue,
        Integer rate,
        QuestionType type,
        String imageUrl
    ) {
        return Rating.builder()
            .surveyId(surveyId)
            .order(order)
            .title(title)
            .description(description)
            .isRequired(isRequired)
            .section(section)
            .maxValue(maxValue)
            .minValue(minValue)
            .rate(rate)
            .type(type.name())
            .imageUrl(imageUrl)
            .build();
    }

    public void updateQuestion(
        String title,
        String description,
        Boolean isRequired,
        Integer order,
        Integer section,
        String maxValue,
        String minValue,
        Integer rate,
        String imageUrl
    ) {
        super.updateQuestion(title, description, isRequired, order, section, imageUrl);
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.rate = rate;
    }
}
