package OneQ.OnSurvey.global.infra.toss.common.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record TossReissueRequest(
        @Schema(description = "refreshToken", example = "리프레쉬 토큰")
        String refreshToken
) {}
