package OneQ.OnSurvey.global.infra.toss.common.dto.iap;

public record OrderStatusResponse(
        String orderId,
        String sku,
        String status,
        String reason,
        String statusDeterminedAt
) {
    public static OrderStatusResponse of(
            String orderId, String sku, String status, String reason, String statusDeterminedAt
    ) {
        return new OrderStatusResponse(orderId, sku, status, reason, statusDeterminedAt);
    }
}
