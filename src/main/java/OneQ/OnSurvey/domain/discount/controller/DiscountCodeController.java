package OneQ.OnSurvey.domain.discount.controller;

import OneQ.OnSurvey.global.common.response.SuccessResponse;
import OneQ.OnSurvey.domain.discount.model.request.CreateDiscountCodeRequest;
import OneQ.OnSurvey.domain.discount.model.response.DiscountCodeResponse;
import OneQ.OnSurvey.domain.discount.model.response.ValidateDiscountCodeResponse;
import OneQ.OnSurvey.domain.discount.service.DiscountCodeCommandService;
import OneQ.OnSurvey.domain.discount.service.DiscountCodeQueryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/discount-codes")
@RequiredArgsConstructor
public class DiscountCodeController {

    private final DiscountCodeQueryService discountCodeQueryService;
    private final DiscountCodeCommandService discountCodeCommandService;

    @GetMapping("/{code}")
    @Operation(summary = "할인 코드를 검증하고 할인 정보를 반환합니다.")
    public SuccessResponse<ValidateDiscountCodeResponse> validate(@PathVariable String code) {
        return SuccessResponse.ok(discountCodeQueryService.validate(code));
    }

    @PostMapping
    @Operation(summary = "할인 코드를 생성합니다.")
    public SuccessResponse<DiscountCodeResponse> create(
            @RequestBody @Valid CreateDiscountCodeRequest request
    ) {
        return SuccessResponse.ok(discountCodeCommandService.create(request));
    }

    @GetMapping
    @Operation(summary = "전체 할인 코드 목록을 조회합니다.")
    public SuccessResponse<List<DiscountCodeResponse>> findAll() {
        return SuccessResponse.ok(discountCodeQueryService.findAll());
    }
}
