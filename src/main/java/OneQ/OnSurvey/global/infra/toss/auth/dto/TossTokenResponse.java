package OneQ.OnSurvey.global.infra.toss.auth.dto;

public record TossTokenResponse(
        String accessToken,
        String refreshToken,
        Long expiresIn,
        String tokenType,
        String scope
) {}
