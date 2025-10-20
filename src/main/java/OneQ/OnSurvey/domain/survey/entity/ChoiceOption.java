package OneQ.OnSurvey.domain.survey.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity
@Table(name = "choice_option")
public class ChoiceOption {
    @Id @Column(name = "choice_option_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "choice_option_seq")
    @SequenceGenerator(name = "choice_option_seq")
    private Long choiceOptionId;

    @Column(name = "question_id")
    private Long questionId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "next_question_id")
    private Long nextQuestionId;
}