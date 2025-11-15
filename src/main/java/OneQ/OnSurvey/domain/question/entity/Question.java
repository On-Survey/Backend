package OneQ.OnSurvey.domain.question.entity;

import OneQ.OnSurvey.domain.question.model.QuestionType;
import OneQ.OnSurvey.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

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

    @Column(name = "type", updatable = false)
    protected QuestionType type;

    @Column(length = 64)
    protected String title;

    @Column(length = 128)
    protected String description;

    @Column(name = "is_required")
    @Builder.Default
    protected Boolean isRequired = false;

    public void updateQuestion(
        String title,
        String description,
        Boolean isRequired,
        Integer order
    ) {
        this.title = title;
        this.description = description;
        this.isRequired = isRequired;
        this.order = order;
    }

    public void updateOrder(Integer order) {
        this.order = order;
    }
}