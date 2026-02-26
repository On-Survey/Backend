package OneQ.OnSurvey.global.infra.toss.common.dto.push;

public record PushResultResponse (
    Long sentPushCount,
    String sentPushContent
) {

    public static PushResultResponse of(Long sentPushCount, String sentPushContentId) {
        return new PushResultResponse(sentPushCount, sentPushContentId);
    }
}
