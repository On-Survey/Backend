package OneQ.OnSurvey.domain.survey.entity;

import OneQ.OnSurvey.common.entity.BaseEntity;
import OneQ.OnSurvey.domain.survey.model.QuestionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity
@Table(name = "question")
public class Question extends BaseEntity {
    @Id @Column(name = "question_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "question_seq")
    @SequenceGenerator(name = "question_seq")
    private Long questionId;

    @Column(name = "survey_id")
    private Long surveyId;

    @Enumerated(EnumType.STRING)
    private QuestionType type;

    @Column(length = 128)
    private String description;

    @Column(name = "is_required")
    private Boolean isRequired;

    // 객관식 문항 필드
    @Column(name = "is_multiple")
    private Boolean isMultiple;

    @Column(name = "has_none_option")
    private Boolean hasNoneOption;

    @Column(name = "has_custom_input")
    private Boolean hasCustomInput;

    // 주관식 문항 필드
    @Column(name = "max_length")
    private Integer maxLength;

    @Column(name = "min_length")
    private Integer minLength;

    @Column(name = "max_value")
    private Integer maxValue;

    @Column(name = "min_value")
    private Integer minValue;
}