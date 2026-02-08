package OneQ.OnSurvey.global.infra.discord.notifier.dto;

import OneQ.OnSurvey.domain.survey.model.AgeRange;
import OneQ.OnSurvey.domain.survey.model.Gender;

import java.time.LocalDateTime;
import java.util.List;

public record SurveySubmittedAlert(
        long userKey,
        long surveyId,
        String title,
        long totalCoin,
        Integer dueCount,
        LocalDateTime deadline,
        Boolean isFree,
        Gender gender,
        List<AgeRange> ages
) {}
