package OneQ.OnSurvey.global.payment.controller;

import OneQ.OnSurvey.global.auth.custom.CustomUserDetails;
import OneQ.OnSurvey.global.common.response.SuccessResponse;
import OneQ.OnSurvey.global.infra.toss.common.dto.iap.PaymentSummaryResponse;
import OneQ.OnSurvey.global.payment.application.PaymentQueryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/payments")
public class PaymentController {

    private final PaymentQueryService paymentQueryService;

    @GetMapping()
    @Operation(summary = "본인 결제 내역 조회")
    public SuccessResponse<List<PaymentSummaryResponse>> getMyPayments(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userKey = userDetails.getUserKey();
        List<PaymentSummaryResponse> payments = paymentQueryService.getPaymentsByUserKey(userKey);

        return SuccessResponse.ok(payments);
    }
}

