package OneQ.OnSurvey.domain.question.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity @Table(name = "choice_option")
public class ChoiceOption {

    @Id @Column(name = "choice_option_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long choiceOptionId;

    @Column(name = "question_id")
    private Long questionId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "next_question_id")
    private Long nextQuestionId;

    public static ChoiceOption of(
        Long questionId,
        String content,
        Long nextQuestionId
    ) {
        return ChoiceOption.builder()
            .questionId(questionId)
            .content(content)
            .nextQuestionId(nextQuestionId)
            .build();
    }

    public void updateOption(
        String content,
        Long nextQuestionId
    ) {
        this.content = content;
        this.nextQuestionId = nextQuestionId;
    }

    public void updateChoiceOption(String content) {
        this.content = content;
    }

    public void updateNextQuestionId(Long nextQuestionId) {
        this.nextQuestionId = nextQuestionId;
    }
}