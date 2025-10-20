package OneQ.OnSurvey.global.infra.toss.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReissueRequest(
        @Schema(description = "refreshToken", example = "리프레쉬 토큰")
        String refreshToken
) {}
