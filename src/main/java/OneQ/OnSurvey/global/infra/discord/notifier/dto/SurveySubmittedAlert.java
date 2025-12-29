package OneQ.OnSurvey.global.infra.discord.notifier.dto;

public record SurveySubmittedAlert(
        long userKey,
        long surveyId,
        String title,
        long totalCoin,
        Integer dueCount
) {}
