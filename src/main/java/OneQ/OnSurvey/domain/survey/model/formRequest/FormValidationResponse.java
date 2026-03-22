package OneQ.OnSurvey.domain.survey.model.formRequest;

import java.util.List;

public record FormValidationResponse(
    List<Result> results
) {

    public record Result(
        int totalCount,
        int convertible,
        int unconvertible,
        List<Unconvertible> details
    ) { }

    public record Unconvertible(
        String title,
        String type,
        String reason
    ) { }

    public static FormValidationResponse from(FormValidationAndStashResponse dto) {
        List<Result> results = dto.results().stream()
            .map(r -> r.isSuccess()
                ? new Result(
                    r.counts().total(),
                    r.counts().convertible(),
                    r.counts().unconvertible(),
                    r.unconvertibleDetails().stream()
                        .map(u -> new Unconvertible(
                            u.title(),
                            u.type(),
                            u.reason()
                        ))
                    .toList()
                )
                : null
            ).toList();

        return new FormValidationResponse(results);
    }
}
