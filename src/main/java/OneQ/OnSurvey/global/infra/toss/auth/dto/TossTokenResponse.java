package OneQ.OnSurvey.global.infra.toss.auth.dto;

public record TossTokenResponse(
        String accessToken,
        String refreshToken,
        Long expiresIn,
        Long refreshTokenExpiresIn,
        String tokenType,
        String scope
) {}
