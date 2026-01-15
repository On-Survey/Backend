package OneQ.OnSurvey.domain.survey.model.dto;

public record GlobalStats (
    Long totalDueCount,
    Long totalCompletedCount,
    Long totalPromotionCount,
    Long dailyUserCount
){
    public static GlobalStats of(
        Long totalDueCount,
        Long totalCompletedCount,
        Long totalPromotionCount,
        Long dailyUserCount
    ) {
        return new GlobalStats(
            totalDueCount,
            totalCompletedCount,
            totalPromotionCount,
            dailyUserCount
        );
    }
}
