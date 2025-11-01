package OneQ.OnSurvey.domain.survey.entity;

import OneQ.OnSurvey.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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