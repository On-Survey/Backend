package OneQ.OnSurvey.domain.participation.entity;

import OneQ.OnSurvey.domain.participation.entity.id.ResponseId;
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

import java.time.LocalDateTime;

@Getter @Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@IdClass(ResponseId.class)
@Entity @Table(name = "response")
public class Response extends BaseEntity {
    @Id @Column(name = "survey_id")
    private Long surveyId;

    @Id @Column(name = "member_id")
    private Long memberId;

    // 응답 등록(런타임) 시 마감기한을 조회하여 채워넣는 것은 비효율적이므로 배치로 처리하기
    @Column(name = "survey_deadline")
    private LocalDateTime surveyDeadline;

    @Column(name = "is_responded", nullable = false)
    @Builder.Default
    private Boolean isResponded = false;

    @Column(name = "is_screened", nullable = false)
    @Builder.Default
    private Boolean isScreened = false;

    public static Response of(
        Long surveyId,
        Long memberId
    ) {
        return Response.builder()
            .surveyId(surveyId)
            .memberId(memberId)
            .build();
    }

    public void markResponded() {
        this.isResponded = true;
    }

    public void markScreened(boolean screened) {
        this.isScreened = screened;
    }
}

