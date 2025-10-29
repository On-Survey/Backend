package OneQ.OnSurvey.domain.question.entity.question;

import OneQ.OnSurvey.domain.question.entity.Question;
import OneQ.OnSurvey.domain.question.model.QuestionType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter @SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@DiscriminatorValue(value = QuestionType.Values.NPS)
public class NPS extends Question {

    public static NPS of(
        Long surveyId,
        Integer order,
        String title
    ) {
        return NPS.builder()
            .surveyId(surveyId)
            .order(order)
            .title(title)
            .build();
    }

    public void updateNPSQuestion(
        String title,
        String description,
        Boolean isRequired
    ) {
        super.updateQuestion(title, description, isRequired);
    }
}
