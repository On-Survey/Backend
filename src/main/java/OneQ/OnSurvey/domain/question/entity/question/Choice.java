package OneQ.OnSurvey.domain.question.entity.question;

import OneQ.OnSurvey.domain.question.entity.Question;
import OneQ.OnSurvey.domain.question.model.QuestionType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter @SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@DiscriminatorValue(value = QuestionType.Values.CHOICE)
public class Choice extends Question {
    // 객관식 문항 필드
    @Column(name = "max_choice")
    @Builder.Default
    private Integer maxChoice = 1;

    @Column(name = "has_none_option")
    private Boolean hasNoneOption;

    @Column(name = "has_custom_input")
    private Boolean hasCustomInput;

    public static Choice of(
        Long surveyId,
        Integer order,
        String title,
        String description,
        Boolean isRequired,
        Integer maxChoice,
        Boolean hasNoneOption,
        Boolean hasCustomInput,
        QuestionType type
    ) {
        return Choice.builder()
            .surveyId(surveyId)
            .order(order)
            .title(title)
            .description(description)
            .isRequired(isRequired)
            .maxChoice(maxChoice)
            .hasNoneOption(hasNoneOption)
            .hasCustomInput(hasCustomInput)
            .type(type.name())
            .build();
    }

    public void updateQuestion(
        String title,
        String description,
        Boolean isRequired,
        Integer order,
        Integer maxChoice,
        Boolean hasNoneOption,
        Boolean hasCustomInput
    ) {
        super.updateQuestion(title, description, isRequired, order);
        this.maxChoice = maxChoice;
        this.hasNoneOption = hasNoneOption;
        this.hasCustomInput = hasCustomInput;
    }
}
