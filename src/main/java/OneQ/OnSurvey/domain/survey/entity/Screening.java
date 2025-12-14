package OneQ.OnSurvey.domain.survey.entity;

import OneQ.OnSurvey.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter @Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity @Table(name = "screening")
public class Screening extends BaseEntity {

    @Id @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "survey_id")
    private Long surveyId;

    private String content;

    private Boolean answer;

    public static Screening of(
        Long surveyId,
        String content,
        Boolean answer
    ) {
        return Screening.builder()
            .surveyId(surveyId)
            .content(content)
            .answer(answer)
            .build();
    }

    public void updateScreening(
        String content,
        Boolean answer
    ) {
        this.content = content;
        this.answer = answer;
    }
}