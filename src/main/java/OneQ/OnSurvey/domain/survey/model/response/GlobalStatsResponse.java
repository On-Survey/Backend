package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.survey.model.dto.GlobalStats;

public record GlobalStatsResponse(
        Long totalDueCount,
        Long totalCompletedCount,
        Long totalPromotionCount,
        Integer dailyUserCount
) {
    public static GlobalStatsResponse from(GlobalStats stats) {
        return new GlobalStatsResponse(
            stats.totalDueCount(),
            stats.totalCompletedCount(),
            stats.totalPromotionCount(),
            stats.dailyUserCount()
        );
    }
}
