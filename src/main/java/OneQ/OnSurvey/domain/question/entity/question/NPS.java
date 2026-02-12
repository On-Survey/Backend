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
        String title,
        String description,
        Boolean isRequired,
        Integer section,
        Integer nextSection,
        QuestionType type
    ) {
        return NPS.builder()
            .surveyId(surveyId)
            .order(order)
            .title(title)
            .description(description)
            .isRequired(isRequired)
            .type(type.name())
            .section(section)
            .nextSection(nextSection)
            .build();
    }

    public void updateQuestion(
        String title,
        String description,
        Boolean isRequired,
        Integer order,
        Integer section,
        Integer nextSection
    ) {
        super.updateQuestion(title, description, isRequired, order, section, nextSection);
    }
}
