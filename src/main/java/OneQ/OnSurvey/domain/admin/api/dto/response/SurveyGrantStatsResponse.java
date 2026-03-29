package OneQ.OnSurvey.domain.admin.api.dto.response;

import java.time.LocalDateTime;

public record SurveyGrantStatsResponse(
        Long surveyId,
        long totalCount,
        long successCount,
        long failedCount,
        long pendingCount,
        LocalDateTime latestAt
) {
}
