package OneQ.OnSurvey.global.infra.toss.common.dto.promotion;

public record ExecutionResultResponse(String status) {
    public boolean isSuccess() { return "SUCCESS".equals(status); }

    public static ExecutionResultResponse success() { return new ExecutionResultResponse("SUCCESS"); }
    public static ExecutionResultResponse pending() { return new ExecutionResultResponse("PENDING"); }
}
