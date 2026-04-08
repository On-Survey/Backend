package OneQ.OnSurvey.global.promotion;

import java.time.LocalDateTime;

public record PromotionGrantStatsProjection(
        Long surveyId,
        long totalCount,
        long successCount,
        long failedCount,
        long pendingCount,
        LocalDateTime latestAt
) {
}
