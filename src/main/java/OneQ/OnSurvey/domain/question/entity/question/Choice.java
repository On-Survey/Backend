package OneQ.OnSurvey.domain.question.entity.question;

import OneQ.OnSurvey.domain.question.entity.Question;
import OneQ.OnSurvey.domain.question.model.QuestionType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;

@Getter @SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@DiscriminatorValue(value = QuestionType.Values.CHOICE)
public class Choice extends Question {

    // 객관식 문항 필드
    @Column(name = "max_choice")
    @ColumnDefault("1")
    @Builder.Default
    private Integer maxChoice = 1;

    @Column(name = "has_none_option")
    @ColumnDefault("FALSE")
    @Builder.Default
    private Boolean hasNoneOption = false;

    @Column(name = "has_custom_input")
    @ColumnDefault("FALSE")
    @Builder.Default
    private Boolean hasCustomInput = false;

    @Column(name = "IS_SECTION_DECIDABLE")
    @ColumnDefault("FALSE")
    @Builder.Default
    private Boolean isSectionDecidable = false;

    public static Choice of(
        Long surveyId,
        Integer order,
        String title,
        String description,
        Boolean isRequired,
        Integer section,
        Integer maxChoice,
        Boolean hasNoneOption,
        Boolean hasCustomInput,
        Boolean isSectionDecidable,
        QuestionType type
    ) {
        return Choice.builder()
            .surveyId(surveyId)
            .order(order)
            .title(title)
            .description(description)
            .isRequired(isRequired)
            .section(section)
            .maxChoice(maxChoice)
            .hasNoneOption(hasNoneOption)
            .hasCustomInput(hasCustomInput)
            .isSectionDecidable(isSectionDecidable)
            .type(type.name())
            .build();
    }

    public void updateQuestion(
        String title,
        String description,
        Boolean isRequired,
        Integer order,
        Integer section,
        Integer maxChoice,
        Boolean hasNoneOption,
        Boolean hasCustomInput,
        Boolean isSectionDecidable
    ) {
        super.updateQuestion(title, description, isRequired, order, section);
        this.maxChoice = maxChoice;
        this.hasNoneOption = hasNoneOption;
        this.hasCustomInput = hasCustomInput;
        this.isSectionDecidable = isSectionDecidable;
    }
}
