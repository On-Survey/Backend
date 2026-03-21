package OneQ.OnSurvey.domain.discount.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateDiscountCodeRequest(
        @NotBlank
        @Schema(description = "학회(기관) 이름", example = "onsurvey")
        String organizationName,

        @NotNull
        @FutureOrPresent
        @Schema(description = "코드 만료 기한", example = "2026-12-31")
        LocalDate expiredAt
) {
}
