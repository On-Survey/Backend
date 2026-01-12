package OneQ.OnSurvey.global.payment.application;

import OneQ.OnSurvey.global.infra.toss.common.dto.iap.IapStatsResponse;
import OneQ.OnSurvey.global.infra.toss.common.dto.iap.OrderStatusResponse;

public interface IapUseCase {

    /**
     * 토스 IAP 주문을 검증하고, 유효하면 우리 도메인에 결제 반영
     * @return 지급 성공 여부
     */
    boolean grantByOrder(long userKey, String orderId, Long price);

    /**
     * 내 토스 IAP 통계 조회
     */
    IapStatsResponse getMyIapStats(long userKey);

    OrderStatusResponse getStatus(long userKey, String orderId);
}
