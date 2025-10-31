package OneQ.OnSurvey.domain.question.entity.question;

import OneQ.OnSurvey.domain.question.entity.Question;
import OneQ.OnSurvey.domain.question.model.QuestionType;
import OneQ.OnSurvey.domain.question.model.TextType;
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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@DiscriminatorValue(value = QuestionType.Values.TEXT)
public class Text extends Question {
    // 주관식 문항 필드
    @Column(name = "text_type")
    private TextType textType;

    @Column(name = "default_value")
    private String defaultValue;

    public static Text of(
        Long surveyId,
        Integer order,
        String title,
        String description,
        Boolean isRequired,
        TextType textType
    ) {
        return Text.builder()
            .surveyId(surveyId)
            .order(order)
            .title(title)
            .description(description)
            .isRequired(isRequired)
            .textType(textType)
            .build();
    }

    public void updateQuestion(
        String title,
        String description,
        Boolean isRequired,
        Integer order,
        String defaultValue
    ) {
        super.updateQuestion(title, description, isRequired, order);
        this.defaultValue = defaultValue;
    }
}
