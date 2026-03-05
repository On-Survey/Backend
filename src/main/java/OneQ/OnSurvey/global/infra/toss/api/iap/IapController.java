package OneQ.OnSurvey.global.infra.toss.api.iap;

import OneQ.OnSurvey.global.auth.custom.CustomUserDetails;
import OneQ.OnSurvey.global.common.response.SuccessResponse;
import OneQ.OnSurvey.global.infra.toss.common.dto.iap.IapGrantRequest;
import OneQ.OnSurvey.global.infra.toss.common.dto.iap.IapStatsResponse;
import OneQ.OnSurvey.global.infra.toss.common.dto.iap.OrderStatusResponse;
import OneQ.OnSurvey.global.payment.application.IapUseCase;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/toss/iap")
@RequiredArgsConstructor
public class IapController {

    private final IapUseCase iapUseCase;

    @PostMapping("/grant")
    @Operation(summary = "IAP 상품 지급(결제 반영)", description = "orderId 서버검증 후 우리 도메인(PAYMENT)에 결제 반영. 멱등 처리됨.")
    public SuccessResponse<Boolean> grant(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody IapGrantRequest body
    ) {
        long userKey = principal.getUserKey();
        return SuccessResponse.ok(iapUseCase.grantByOrder(userKey, body.orderId(), body.price()));
    }

    @PostMapping("/grant-home")
    @Operation(summary = "홈 결제 반영", description = "orderId 서버검증 후 PAYMENT 반영 (코인 지급 없음)")
    public SuccessResponse<Boolean> grantHome(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody IapGrantRequest body
    ) {
        long userKey = principal.getUserKey();
        return SuccessResponse.ok(iapUseCase.grantHomeByOrder(userKey, body.orderId(), body.price()));
    }

    /** 추가 컨트롤러. 삭제 고려 **/
    @PostMapping("/status")
    @Operation(summary = "IAP 주문 상태 조회", description = "주문 상태 재검증")
    public SuccessResponse<OrderStatusResponse> status(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody IapGrantRequest body
    ) {
        long userKey = principal.getUserKey();
        return SuccessResponse.ok(iapUseCase.getStatus(userKey, body.orderId()));
    }

    @GetMapping("/stats")
    @Operation(summary = "내 IAP 결제 통계", description = "현재 로그인 유저의 IAP 결제 총 횟수/총 금액(성공 결제 기준)")
    public SuccessResponse<IapStatsResponse> stats(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        long userKey = principal.getUserKey();
        return SuccessResponse.ok(iapUseCase.getMyIapStats(userKey));
    }
}
