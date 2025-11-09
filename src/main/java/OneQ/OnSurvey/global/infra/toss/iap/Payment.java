package OneQ.OnSurvey.global.infra.toss.iap;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PAYMENT_ID")
    private Long id;

    @Column(name = "SURVEY_ID", nullable = false)
    private Long surveyId;

    @Column(name = "ORDER_ID", length = 50, nullable = false)
    private String orderId;

    @Column(name = "SKU", length = 100)
    private String sku;

    @Column(name = "USER_KEY", nullable = false)
    private Long userKey;

    @Column(name = "PAYMENT_COMPLETED_AT")
    private LocalDateTime paymentCompletedAt;

    @Column(name = "TOTAL_AMOUNT", nullable = false)
    private Integer totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "REFUND_AT")
    private LocalDateTime refundAt;

    public void markPaid(LocalDateTime completedAt) {
        this.status = PaymentStatus.PAID;
        this.paymentCompletedAt = completedAt != null ? completedAt : LocalDateTime.now();
    }

    public void markRefunded(LocalDateTime refundedAt) {
        this.status = PaymentStatus.REFUNDED;
        this.refundAt = refundedAt != null ? refundedAt : LocalDateTime.now();
    }

    public static Payment pending(Long surveyId, Long userKey, String orderId, String sku, Integer totalAmount) {
        return Payment.builder()
                .surveyId(surveyId)
                .userKey(userKey)
                .orderId(orderId)
                .sku(sku)
                .totalAmount(totalAmount != null ? totalAmount : 0)
                .status(PaymentStatus.PENDING)
                .build();
    }
}
