package OneQ.OnSurvey.domain.survey.entity;

import OneQ.OnSurvey.common.entity.BaseEntity;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
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
@Table(name = "survey")
public class Survey extends BaseEntity {
    @Id @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "survey_id")
    @SequenceGenerator(name = "survey_id")
    private Long id;

    @Column(name = "member_id")
    private Long memberId;

    @Column(length = 64)
    private String title;

    private Boolean isTemporary;

    @Enumerated(EnumType.STRING)
    private SurveyStatus status;
}