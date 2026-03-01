package OneQ.OnSurvey.domain.survey.model.formRequest;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record FormConversionResponse(
    int totalCount,
    int successCount,
    List<Result> results,
    String error
) {

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Result(
        String url,
        String status, // "SUCCESS", "FAIL"

        // SUCCESS
        Survey survey,
        List<UnsupportedQuestion> unsupportedQuestions,

        // FAIL
        String message

    ) {
        public boolean isSuccess() {
            return "SUCCESS".equals(status);
        }
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Survey(
        String title,
        String description,
        List<Section> sections
    ) { }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Section(
        String id,
        int order,
        String title,
        String description,
        Integer nextSectionOrder,
        List<Question> questions
    ) { }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Question(
        String id,
        String title,
        String description,
        String type,
        boolean required,
        List<Option> options
    ) { }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Option(
        String text,
        Integer goToSectionOrder,
        boolean isOther
    ) { }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record UnsupportedQuestion(
        int order,
        String title,
        String type,
        String reason
    ) { }
}