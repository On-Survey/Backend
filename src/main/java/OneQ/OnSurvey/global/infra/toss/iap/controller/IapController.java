package OneQ.OnSurvey.global.infra.toss.iap.controller;

import OneQ.OnSurvey.global.auth.custom.CustomUserDetails;
import OneQ.OnSurvey.global.infra.toss.iap.service.IapService;
import OneQ.OnSurvey.global.infra.toss.iap.dto.IapGrantRequest;
import OneQ.OnSurvey.global.infra.toss.iap.dto.OrderStatusResponse;
import OneQ.OnSurvey.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/toss/iap")
@RequiredArgsConstructor
public class IapController {

    private final IapService iapService;

    @PostMapping("/grant")
    @Operation(summary = "IAP 상품 지급(결제 반영)", description = "orderId 서버검증 후 우리 도메인(PAYMENT)에 결제 반영. 멱등 처리됨.")
    public SuccessResponse<Boolean> grant(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody IapGrantRequest body
    ) {
        long userKey = principal.getUserKey();
        return SuccessResponse.ok(iapService.grantByOrder(userKey, body.orderId(), body.surveyId(), body.price()));
    }

    /** 추가 컨트롤러. 삭제 고려 **/
    @PostMapping("/status")
    @Operation(summary = "IAP 주문 상태 조회", description = "주문 상태 재검증")
    public SuccessResponse<OrderStatusResponse> status(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody IapGrantRequest body
    ) {
        long userKey = principal.getUserKey();
        return SuccessResponse.ok(iapService.getStatus(userKey, body.orderId()));
    }
}
