package OneQ.OnSurvey.domain.survey.model.formRequest;

import java.util.List;

public record FormValidationAndStashResponse(
    int totalUrls,
    int successCount,
    List<Result> results
) {

    public record Result(
        String url,
        String status, // "SUCCESS", "FAIL"

        // SUCCESS
        Count counts,
        List<Unconvertible> unconvertibleDetails,

        // FAIL
        String message
    ) {
        public boolean isSuccess() {
            return "SUCCESS".equals(status);
        }
    }

    public record Count(
        int total,
        int convertible,
        int unconvertible
    ) { }

    public record Unconvertible(
        String title,
        String type,
        int order,
        String reason
    ) { }
}
