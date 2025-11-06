package OneQ.OnSurvey.global.infra.toss.dto;

public record ExecutionResultResponse(String status) {
    public boolean isSuccess() { return "SUCCESS".equals(status); }
    public boolean isPending() { return "PENDING".equals(status); }
}
