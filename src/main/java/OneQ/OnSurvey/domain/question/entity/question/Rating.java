package OneQ.OnSurvey.domain.question.entity.question;

import OneQ.OnSurvey.domain.question.entity.Question;
import OneQ.OnSurvey.domain.question.model.QuestionType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter @SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@DiscriminatorValue(value = QuestionType.Values.RATING)
public class Rating extends Question {

    public Rating createRatingQuestion(
        Long surveyId,
        Integer order,
        String title
    ) {
        return Rating.builder()
            .surveyId(surveyId)
            .order(order)
            .title(title)
            .build();
    }

    public void updateRatingQuestion(
        String title,
        String description,
        Boolean isRequired
    ) {
        super.updateQuestion(title, description, isRequired);
    }
}
