package OneQ.OnSurvey.global.infra.toss.common.dto.auth;

public record TossTokenResponse(
        String accessToken,
        String refreshToken,
        Long expiresIn,
        String tokenType,
        String scope
) {}
