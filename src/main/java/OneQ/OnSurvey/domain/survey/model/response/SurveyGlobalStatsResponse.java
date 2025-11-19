package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.survey.entity.SurveyGlobalStats;

public record SurveyGlobalStatsResponse(
        Long totalDueCount,
        Long totalCompletedCount,
        Long totalPromotionCount
) {
    public static SurveyGlobalStatsResponse from(SurveyGlobalStats stats) {
        return new SurveyGlobalStatsResponse(
                stats.getTotalDueCount(),
                stats.getTotalCompletedCount(),
                stats.getTotalPromotionCount()
        );
    }
}
