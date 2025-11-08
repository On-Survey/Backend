package OneQ.OnSurvey.global.infra.toss.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record GetOrderStatusRequest(
        @Schema(description = "주문 ID", example = "orderId")
        @NotBlank String orderId
) {}
