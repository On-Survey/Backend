package OneQ.OnSurvey.domain.question.entity.question;

import OneQ.OnSurvey.domain.question.entity.Question;
import OneQ.OnSurvey.domain.question.model.QuestionType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;

@Getter @SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@DiscriminatorValue(value = QuestionType.Values.TIME)
public class Time extends Question {

    @Column(name = "IS_INTERVAL")
    @ColumnDefault("FALSE")
    @Builder.Default
    private Boolean isInterval = false;

    public static Time of(
        Long surveyId,
        Integer order,
        String title,
        String description,
        Boolean isRequired,
        Integer section,
        QuestionType type,
        String imageUrl,
        Boolean isInterval
    ) {
        return Time.builder()
            .surveyId(surveyId)
            .order(order)
            .title(title)
            .description(description)
            .isRequired(isRequired)
            .type(type.name())
            .section(section)
            .imageUrl(imageUrl)
            .isInterval(isInterval)
            .build();
    }

    public void updateQuestion(
        String title,
        String description,
        Boolean isRequired,
        Integer order,
        Integer section,
        String imageUrl,
        Boolean isInterval
    ) {
        super.updateQuestion(title, description, isRequired, order, section, imageUrl);
        this.isInterval = isInterval;
    }
}
