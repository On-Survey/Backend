package OneQ.OnSurvey.domain.question.entity.question;

import OneQ.OnSurvey.domain.question.entity.Question;
import OneQ.OnSurvey.domain.question.model.QuestionType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@DiscriminatorValue(value = QuestionType.Values.TITLE)
public class Title extends Question {

    public static Title of(
        Long surveyId,
        Integer order,
        String title,
        String description,
        Integer section,
        QuestionType type
    ) {
        return Title.builder()
            .surveyId(surveyId)
            .order(order)
            .title(title)
            .description(description)
            .isRequired(false)
            .type(type.name())
            .section(section)
            .imageUrl(null)
            .build();
    }

    public void updateQuestion(
        String title,
        String description,
        Integer order,
        Integer section
    ) {
        super.updateQuestion(title, description, false, order, section, null);
    }
}
