package OneQ.OnSurvey.domain.survey.model.formRequest;

import java.util.List;

public record FormConversionResponse(
    int totalCount,
    int successCount,
    List<Result> results,
    String error
) {

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

    public record Survey(
        String title,
        String description,
        List<Section> sections
    ) { }

    public record Section(
        String id,
        int order,
        String title,
        String description,
        Integer nextSectionOrder,
        List<Question> questions
    ) { }

    public record Question(
        String id,
        String title,
        String description,
        String type,
        boolean required,
        List<Option> options
    ) { }

    public record Option(
        String text,
        Integer goToSectionOrder,
        boolean isOther
    ) { }

    public record UnsupportedQuestion(
        int order,
        String title,
        String type,
        String reason
    ) { }
}