package OneQ.OnSurvey.domain.survey.entity;

import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Entity @Table(name = "survey")
public class Survey extends BaseEntity {
    @Id @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "survey_id")
    @SequenceGenerator(name = "survey_id")
    private Long id;

    @Column(name = "member_id")
    private Long memberId;

    @Column(length = 32)
    private String title;

    @Column(length = 50)
    private String description;

    @Column(name = "is_temporary")
    @Builder.Default
    private Boolean isTemporary = true;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private SurveyStatus status = SurveyStatus.WRITING;

    public static Survey of(
        Long memberId,
        String title,
        String description
    ) {
        return Survey.builder()
            .memberId(memberId)
            .title(title)
            .description(description)
            .build();
    }

    public void saveSurvey() {
        this.isTemporary = false;
        this.status = SurveyStatus.REVIEW;
    }

    public void updateSurveyStatus(SurveyStatus status) {
        this.status = status;
    }

    public void updateSurveyTitleAndDescription(
        String title,
        String description
    ) {
        this.title = title;
        this.description = description;
    }
}