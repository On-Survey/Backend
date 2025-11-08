package OneQ.OnSurvey.global.infra.toss.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TossLoginRequest(
        @Schema(description = "authorizationCode", example = "인가코드")
        String authorizationCode,
        @Schema(description = "referrer", example = "referrer")
        String referrer
) {}
