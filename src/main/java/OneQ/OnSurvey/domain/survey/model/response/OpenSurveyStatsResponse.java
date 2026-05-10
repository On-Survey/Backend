package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.survey.model.dto.OpenSurveyStats;

public record OpenSurveyStatsResponse(
    Long openSurveyCount,
    Integer maxRewardCoin
) {
    public static OpenSurveyStatsResponse from(OpenSurveyStats stats) {
        return new OpenSurveyStatsResponse(stats.openSurveyCount(), stats.maxRewardCoin());
    }
}
