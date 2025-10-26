package OneQ.OnSurvey.domain.question.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity @Table(name = "choice_option")
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

    public ChoiceOption createChoiceOption(
        Long questionId,
        String content
    ) {
        return ChoiceOption.builder()
            .questionId(questionId)
            .content(content)
            .build();
    }

    public void updateChoiceOption(String content) {
        this.content = content;
    }

    public void updateNextQuestionId(Long nextQuestionId) {
        this.nextQuestionId = nextQuestionId;
    }
}