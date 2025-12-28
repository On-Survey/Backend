package OneQ.OnSurvey.global.infra.discord.notifier.dto;

public record PaymentCompletedAlert(
        long userKey,
        String orderId,
        long amount,
        String paidAt,
        long newBalance
) {}