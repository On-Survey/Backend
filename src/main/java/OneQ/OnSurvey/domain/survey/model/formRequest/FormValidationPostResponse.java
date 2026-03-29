package OneQ.OnSurvey.domain.survey.model.formRequest;

import OneQ.OnSurvey.domain.question.model.dto.SectionDto;
import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;

import java.util.List;

public record FormValidationPostResponse(
    int totalUrls,
    int successCount,
    int emailSent,
    List<Result> results
) {

    public record Result(
        String url,
        String status, // "SUCCESS", "FAIL"

        // SUCCESS
        Count counts,
        List<Inconvertible> inconvertibleDetails,
        Convertible convertibleDetails,

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
        int inconvertible
    ) { }

    public record Inconvertible(
        String title,
        String type,
        String reason
    ) { }

    public record Convertible(
        String title,
        String description,
        List<SectionDto> sections,
        List<DefaultQuestionDto> questions
    ) { }
}
