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

    @Column(name = "max_length")
    private Integer maxLength;

    @Column(name = "min_length")
    private Integer minLength;

    @Column(name = "max_value")
    private Integer maxValue;

    @Column(name = "min_value")
    private Integer minValue;

    public Text createTextQuestion(
        Long surveyId,
        Integer order,
        TextType textType,
        String title
    ) {
        return Text.builder()
            .surveyId(surveyId)
            .order(order)
            .textType(textType)
            .title(title)
            .build();
    }

    public void updateTextQuestion(
        String title,
        String description,
        Boolean isRequired
    ) {
        super.updateQuestion(title, description, isRequired);
    }
}
