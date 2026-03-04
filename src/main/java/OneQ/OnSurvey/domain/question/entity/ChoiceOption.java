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

    @Column(name = "next_section")
    private Integer nextSection;

    @Column(name = "image_url")
    private String imageUrl;

    public static ChoiceOption of(
        Long questionId,
        String content,
        Integer nextSection,
        String imageUrl
    ) {
        return ChoiceOption.builder()
            .questionId(questionId)
            .content(content)
            .nextSection(nextSection)
            .imageUrl(imageUrl)
            .build();
    }

    public void updateOption(
        String content,
        Integer nextSection,
        String imageUrl
    ) {
        this.content = content;
        this.nextSection = nextSection;
        this.imageUrl = imageUrl;
    }

    public void updateChoiceOption(String content) {
        this.content = content;
    }

    public void updateNextSection(Integer nextSection) {
        this.nextSection = nextSection;
    }
}