package OneQ.OnSurvey.global.infra.discord.notifier.dto;

public record TossAccessTokenAlert (
    String accessToken,
    String errorCode,
    String errorReason
) {
}
