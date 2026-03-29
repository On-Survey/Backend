package OneQ.OnSurvey.domain.admin.api.dto.response;

import OneQ.OnSurvey.global.promotion.PromotionGrantStatsProjection;

import java.time.LocalDateTime;

public record SurveyGrantStatsResponse(
        Long surveyId,
        long totalCount,
        long successCount,
        long failedCount,
        long pendingCount,
        LocalDateTime latestAt
) {
    public static SurveyGrantStatsResponse from(PromotionGrantStatsProjection p) {
        return new SurveyGrantStatsResponse(
                p.surveyId(), p.totalCount(), p.successCount(),
                p.failedCount(), p.pendingCount(), p.latestAt()
        );
    }
}
