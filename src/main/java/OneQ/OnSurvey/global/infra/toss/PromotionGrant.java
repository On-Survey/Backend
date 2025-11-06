package OneQ.OnSurvey.global.infra.toss;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "promotion_grant",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_grant_user_survey_code",
                columnNames = {"user_key", "survey_id", "promotion_code"}
        )
)
@Getter
public class PromotionGrant {

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

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static PromotionGrant of(Long userKey, Long surveyId, String promotionCode) {
        PromotionGrant promotionGrant = new PromotionGrant();
        promotionGrant.userKey = userKey;
        promotionGrant.surveyId = surveyId;
        promotionGrant.promotionCode = promotionCode;
        promotionGrant.status = GrantStatus.PENDING;
        return promotionGrant;
    }

    public void withExecKey(String execKey) {this.execKey = execKey;}

    public void pending() { this.status = GrantStatus.PENDING; }
    public void success() { this.status = GrantStatus.SUCCESS; }
    public void fail() { this.status = GrantStatus.FAILED; }

    public boolean isSuccess() { return this.status == GrantStatus.SUCCESS; }
    public boolean isPending() { return this.status == GrantStatus.PENDING; }
}
