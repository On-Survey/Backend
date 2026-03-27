package OneQ.OnSurvey.global.infra.discord.notifier.dto;

import java.util.List;

public record SurveyHelpRequestAlert(
        String email,
        String name,
        List<String> rejectionReasons,
        String content
) {}
