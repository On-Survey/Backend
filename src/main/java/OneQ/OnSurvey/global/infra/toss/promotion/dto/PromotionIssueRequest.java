package OneQ.OnSurvey.global.infra.toss.promotion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record PromotionIssueRequest(
        @Schema(description = "설문 ID", example = "1")
        @NotNull Long surveyId
) {}
