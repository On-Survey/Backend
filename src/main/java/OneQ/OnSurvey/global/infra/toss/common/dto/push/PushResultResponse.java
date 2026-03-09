package OneQ.OnSurvey.global.infra.toss.common.dto.push;

public record PushResultResponse (
    Long sentPushCount,
    String sentPushContentIds
) {

    public static PushResultResponse of(Long sentPushCount, String sentPushContentIds) {
        return new PushResultResponse(sentPushCount, sentPushContentIds);
    }
}
