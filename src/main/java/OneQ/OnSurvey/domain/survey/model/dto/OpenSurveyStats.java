package OneQ.OnSurvey.domain.survey.model.dto;

public record OpenSurveyStats(
    Long openSurveyCount,
    Integer maxRewardCoin
) {
    public static OpenSurveyStats of(Long openSurveyCount, Integer maxRewardCoin) {
        return new OpenSurveyStats(
            openSurveyCount != null ? openSurveyCount : 0L,
            maxRewardCoin != null ? maxRewardCoin : 0
        );
    }
}
