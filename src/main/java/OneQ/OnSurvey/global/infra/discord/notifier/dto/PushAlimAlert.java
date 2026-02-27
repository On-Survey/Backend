package OneQ.OnSurvey.global.infra.discord.notifier.dto;

public record PushAlimAlert(
    long userKey,
    String templateSetCode,
    long completedCount,
    long failedCount,
    String errorReason
) {
}
