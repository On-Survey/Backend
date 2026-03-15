package OneQ.OnSurvey.domain.discount.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateDiscountCodeRequest(
        @NotBlank
        @Schema(description = "학회(기관) 이름", example = "onsurvey")
        String organizationName
) {
}
