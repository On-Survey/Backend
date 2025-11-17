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
import lombok.Builder;
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

    // 응답 등록(런타임) 시 마감기한을 조회하여 채워넣는 것은 비효율적이므로 배치로 처리하기
    @Column(name = "survey_deadline")
    private LocalDateTime surveyDeadline;

    @Column(name = "is_responded")
    private Boolean isResponded;

    @Builder.Default
    @Column(name = "is_screening_rejected")
    private Boolean isScreeningRejected = false;

    public boolean isExcluded() {
        return this.isResponded || !this.isScreeningRejected;
    }

    public static MemberSurveyStatus of(
        Long surveyId,
        Long memberId,
        Boolean isResponded
    ) {
        return MemberSurveyStatus.builder()
            .surveyId(surveyId)
            .memberId(memberId)
            .isResponded(isResponded)
            .build();
    }

    public static MemberSurveyStatus of(
        Long surveyId,
        Long memberId,
        Boolean isResponded,
        Boolean isScreeningRejected
    ) {
        return MemberSurveyStatus.builder()
            .surveyId(surveyId)
            .memberId(memberId)
            .isResponded(isResponded)
            .isScreeningRejected(isScreeningRejected)
            .build();
    }

    public void updateResponseStatus(Boolean isResponded) {
        this.isResponded = isResponded;
    }
}
