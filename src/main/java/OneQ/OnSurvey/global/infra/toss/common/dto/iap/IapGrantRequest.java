package OneQ.OnSurvey.global.infra.toss.common.dto.iap;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record IapGrantRequest(
        @Schema(description = "주문 ID", example = "orderId")
        @NotBlank String orderId,

        @Schema(description = "결제 금액", example = "1000")
        @NotNull Long price
) {}
