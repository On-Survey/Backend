package OneQ.OnSurvey.global.promotion.entity;

import OneQ.OnSurvey.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "promotion_grant",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_grant_user_survey_code",
                columnNames = {"user_key", "survey_id", "promotion_code"}
        )
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PromotionGrant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_key", nullable = false)
    private Long userKey;

    @Column(name = "survey_id", nullable = false)
    private Long surveyId;

    @Column(name = "promotion_code", nullable = false)
    private String promotionCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GrantStatus status;

    @Column(name = "exec_key", length = 128)
    private String execKey;

    @Column(name = "exec_key_issued_at")
    private Instant execKeyIssuedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean pointGranted = false;

    public static PromotionGrant of(Long userKey, Long surveyId, String promotionCode) {
        PromotionGrant promotionGrant = new PromotionGrant();
        promotionGrant.userKey = userKey;
        promotionGrant.surveyId = surveyId;
        promotionGrant.promotionCode = promotionCode;
        promotionGrant.status = GrantStatus.PENDING;
        return promotionGrant;
    }

    public PromotionGrant withExecKey(String key) {
        this.execKey = key;
        this.execKeyIssuedAt = Instant.now();
        return this;
    }

    public void pending() { this.status = GrantStatus.PENDING; }
    public void success() { this.status = GrantStatus.SUCCESS; }
    public void fail() { this.status = GrantStatus.FAILED; }

    public boolean isSuccess() { return this.status == GrantStatus.SUCCESS; }
    public boolean isPending() { return this.status == GrantStatus.PENDING; }

    public void markPointGranted() {
        this.pointGranted = true;
    }
}
