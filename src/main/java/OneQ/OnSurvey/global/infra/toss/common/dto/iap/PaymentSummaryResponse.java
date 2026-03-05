package OneQ.OnSurvey.global.infra.toss.common.dto.iap;

import OneQ.OnSurvey.global.common.annotation.DateFormat;
import OneQ.OnSurvey.global.payment.entity.Payment;

import java.time.LocalDateTime;

public record PaymentSummaryResponse(
        Long paymentId,
        @DateFormat LocalDateTime paymentDate,
        Integer totalAmount,
        String orderId
) {
    public static PaymentSummaryResponse fromEntity(Payment payment) {
        return new PaymentSummaryResponse(
                payment.getId(),
                payment.getPaymentCompletedAt(),
                payment.getTotalAmount(),
                payment.getOrderId()
        );
    }
}
