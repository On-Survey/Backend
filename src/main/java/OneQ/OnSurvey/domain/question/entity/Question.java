package OneQ.OnSurvey.domain.question.entity;

import OneQ.OnSurvey.domain.question.model.QuestionType;
import OneQ.OnSurvey.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;

@Getter @SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity @Table(name = "question")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public abstract class Question extends BaseEntity {

    @Id @Column(name = "question_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @Column(name = "survey_id")
    private Long surveyId;

    @Column(name = "QUESTION_ORDER")
    protected Integer order;

    @Column(name = "SECTION")
    @ColumnDefault("1")
    @Builder.Default
    protected Integer section = 1;

    @Column(name = "NEXT_SECTION")
    protected Integer nextSection;

    @Column(name = "type", insertable = false, updatable = false)
    protected String type;

    @Column(columnDefinition = "TEXT")
    protected String title;

    @Column(columnDefinition = "TEXT")
    protected String description;

    @Column(name = "is_required")
    @ColumnDefault("FALSE")
    @Builder.Default
    protected Boolean isRequired = false;

    public void updateQuestion(
        String title,
        String description,
        Boolean isRequired,
        Integer order,
        Integer section,
        Integer nextSection
    ) {
        this.title = title;
        this.description = description;
        this.isRequired = isRequired;
        this.order = order;
        this.section = (section != null) ? section : this.section;
        this.nextSection = nextSection;
    }

    public void updateOrder(Integer order) {
        this.order = order;
    }

    public boolean isChoice() {
        return QuestionType.CHOICE.equals(QuestionType.valueOf(this.type));
    }
}