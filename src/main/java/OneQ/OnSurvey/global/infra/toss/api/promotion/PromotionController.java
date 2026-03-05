package OneQ.OnSurvey.global.infra.toss.api.promotion;

import OneQ.OnSurvey.global.auth.custom.CustomUserDetails;
import OneQ.OnSurvey.global.common.response.SuccessResponse;
import OneQ.OnSurvey.global.infra.toss.common.dto.promotion.ExecutionResultResponse;
import OneQ.OnSurvey.global.infra.toss.common.dto.promotion.PromotionIssueRequest;
import OneQ.OnSurvey.global.infra.toss.common.dto.promotion.recheck.PromotionRecheckPendingRequest;
import OneQ.OnSurvey.global.infra.toss.common.dto.promotion.recheck.PromotionRecheckPendingResponse;
import OneQ.OnSurvey.global.promotion.application.PromotionRecheckService;
import OneQ.OnSurvey.global.promotion.application.PromotionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/toss/promotion")
@RequiredArgsConstructor
@Tag(name = "프로모션(토스포인트)")
public class PromotionController {

    private final PromotionUseCase promotionUseCase;
    private final PromotionRecheckService promotionRecheckService;

    @PostMapping("/issue")
    @Operation(
            summary = "토스포인트 지급 실행",
            description = "프로모션 코드/금액으로 토스 포인트 지급을 실행합니다."
    )
    public SuccessResponse<ExecutionResultResponse> issue(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody @Valid PromotionIssueRequest request
    ) {
        ExecutionResultResponse res = promotionUseCase.issueAndConfirm(principal.getUserKey(), request.surveyId());
        return SuccessResponse.ok(res);
    }

    @PostMapping("/recheck-pending")
    @Operation(
            summary = "[호출 유의] PENDING 프로모션 재조회",
            description = "PENDING 상태로 남아있는 프로모션들을 재조회하여 최신 상태로 갱신합니다."
    )
    public SuccessResponse<PromotionRecheckPendingResponse> recheckPending(
            @RequestBody(required = false) PromotionRecheckPendingRequest request
    ) {
        Integer limit = (request == null) ? null : request.limit();
        PromotionRecheckPendingResponse res = promotionRecheckService.recheckPending(limit);
        return SuccessResponse.ok(res);
    }

}

