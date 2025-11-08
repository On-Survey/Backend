package OneQ.OnSurvey.global.infra.toss.controller;

import OneQ.OnSurvey.global.auth.custom.CustomUserDetails;
import OneQ.OnSurvey.global.infra.toss.dto.ExecutionResultResponse;
import OneQ.OnSurvey.global.infra.toss.dto.PromotionIssueRequest;
import OneQ.OnSurvey.global.infra.toss.service.PromotionService;
import OneQ.OnSurvey.global.response.SuccessResponse;
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

    private final PromotionService promotionService;

    @PostMapping("/issue")
    @Operation(
            summary = "토스포인트 지급 실행",
            description = "프로모션 코드/금액으로 토스 포인트 지급을 실행합니다."
    )
    public SuccessResponse<ExecutionResultResponse> issue(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody @Valid PromotionIssueRequest request
    ) {
        ExecutionResultResponse res = promotionService.issueAndConfirm(principal.getUserKey(), request.surveyId());
        return SuccessResponse.ok(res);
    }
}

