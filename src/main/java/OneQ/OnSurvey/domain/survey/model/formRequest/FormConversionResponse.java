package OneQ.OnSurvey.domain.survey.model.formRequest;

//import java.util.List;
//
//public record FormConversionResponse(
//    Long totalCount,
//    Long successCount,
//    List<Success> results,
//
//    String error
//) {
//
//    public record Success(
//        String url,
//        String status,
//        List<ConvertedSurvey> successSurveyList,
//        List<Failure> failureQuestionList
//    ) { }
//
//    public record ConvertedSurvey(
//        String title,
//        String description,
//        List<Section> sectionList
//    ) {
//
//        public record Section(
//            Integer sectionOrder,
//            String sectionTitle,
//            String sectionDesc,
//            Integer nextSectionOrder,
//            List<Question> questionList
//        ) { }
//
//        public record Question(
//            String questionTitle,
//            String questionDesc,
//            String type,
//            Boolean isRequired,
//            List<Option> options
//        ) {
//            public record Option(
//                String optionText,
//                Integer nextSectionOrder,
//                Boolean isOther
//            ) { }
//        }
//    }
//
//    public record Failure(
//        Integer order,
//        String questionTitle,
//        String type,
//        String reason
//    ) { }
//
//
//    public boolean isAllSuccess() {
//        return totalCount.equals(successCount);
//    }
//}

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