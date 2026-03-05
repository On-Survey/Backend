package OneQ.OnSurvey.global.infra.discord.notifier.dto;

import OneQ.OnSurvey.global.payment.entity.PaymentPurpose;

public record PaymentCompletedAlert(
        long userKey,
        String orderId,
        long amount,
        String paidAt,
        PaymentPurpose paymentPurpose,
        long newBalance
) {}