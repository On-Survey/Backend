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
@DiscriminatorValue(value = QuestionType.Values.GRID)
public class Grid extends Question {

    // 객관식 - 체크박스 그리드
    @Column(name = "IS_CHECKBOX")
    @ColumnDefault("FALSE")
    @Builder.Default
    private Boolean isCheckbox = false;

    // 행 순서가 무작위인 문항
    @Column(name = "IS_CHOICE_MIXED")
    @ColumnDefault("FALSE")
    @Builder.Default
    private Boolean isChoiceMixed = false;

    // 각 열당 최대 1개의 응답만 존재할 수 있는 문항
    @Column(name = "IS_CHOICE_DISTINCT")
    @ColumnDefault("FALSE")
    @Builder.Default
    private Boolean isChoiceDistinct = false;

    public static Grid of(
        Long surveyId,
        Integer order,
        String title,
        String description,
        Boolean isRequired,
        Integer section,
        QuestionType type,
        String imageUrl,
        Boolean isCheckbox,
        Boolean isChoiceMixed,
        Boolean isChoiceDistinct
    ) {
        return Grid.builder()
            .surveyId(surveyId)
            .order(order)
            .title(title)
            .description(description)
            .isRequired(isRequired)
            .type(type.name())
            .section(section)
            .imageUrl(imageUrl)
            .isCheckbox(isCheckbox)
            .isChoiceMixed(isChoiceMixed)
            .isChoiceDistinct(isChoiceDistinct)
            .build();
    }

    public void updateQuestion(
        String title,
        String description,
        Boolean isRequired,
        Integer order,
        Integer section,
        String imageUrl,
        Boolean isCheckbox,
        Boolean isChoiceMixed,
        Boolean isChoiceDistinct
    ) {
        super.updateQuestion(title, description, isRequired, order, section, imageUrl);
        this.isCheckbox = isCheckbox != null ? isCheckbox : false;
        this.isChoiceMixed = isChoiceMixed != null ? isChoiceMixed : false;
        this.isChoiceDistinct = isChoiceDistinct != null ? isChoiceDistinct : false;
    }
}
