package OneQ.OnSurvey.domain.participation.entity;

import OneQ.OnSurvey.domain.participation.entity.id.ResponseId;
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

@Getter @Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@IdClass(ResponseId.class)
@Entity @Table(name = "response")
public class Response extends AbstractAnswer {
    @Id @Column(name = "survey_id")
    private Long surveyId;

    @Id @Column(name = "member_id")
    private Long memberId;

    @Column(name = "is_settled")
    @Builder.Default
    private Boolean isSettled = false;

    public static Response of(
        Long surveyId,
        Long memberId
    ) {
        return Response.builder()
            .surveyId(surveyId)
            .memberId(memberId)
            .build();
    }

    public void settlementCompleted() {
        this.isSettled = true;
    }
}

