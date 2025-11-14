package OneQ.OnSurvey.domain.question.entity.question;

import java.time.LocalDateTime;

import OneQ.OnSurvey.domain.question.entity.Question;
import OneQ.OnSurvey.domain.question.model.QuestionType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter @SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@DiscriminatorValue(value = QuestionType.Values.SHORT)
public class ShortAnswer extends Question {

    public static ShortAnswer of(
        Long surveyId,
        Integer order,
        String title,
        String description,
        Boolean isRequired
    ) {
        return ShortAnswer.builder()
            .surveyId(surveyId)
            .order(order)
            .title(title)
            .description(description)
            .isRequired(isRequired)
            .build();
    }

    public void updateQuestion(
        String title,
        String description,
        Boolean isRequired,
        Integer order
    ) {
        super.updateQuestion(title, description, isRequired, order);
    }
}
