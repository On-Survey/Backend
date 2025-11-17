package OneQ.OnSurvey.domain.participation.entity;

import OneQ.OnSurvey.domain.participation.entity.id.MemberSurveyStatusId;
import OneQ.OnSurvey.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter @SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity @Table(name = "user_survey_status")
@IdClass(MemberSurveyStatusId.class)
public class MemberSurveyStatus extends BaseEntity {

    @Id @Column(name = "survey_id")
    private Long surveyId;

    @Id @Column(name = "member_id")
    private Long memberId;

    @Column(name = "is_responded")
    private Boolean isResponded;

    @Column(name = "is_screening_passed")
    private Boolean isScreeningPassed;

    @Column(name = "survey_deadline")
    private LocalDateTime surveyDeadline;

    public boolean isExcluded() {
        return this.isResponded || !this.isScreeningPassed;
    }
}
