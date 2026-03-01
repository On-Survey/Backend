package OneQ.OnSurvey.global.infra.discord.notifier.dto;

import java.util.List;

public record SurveyConversionAlert(
    int totalCount,
    int successCount,
    List<SurveyDetails> details,
    String error
) {
    public boolean isSuccess() {
        return error == null || error.isBlank();
    }

    public static SurveyConversionAlert success(int totalCount, int successCount, List<SurveyDetails> details) {
        return new SurveyConversionAlert(totalCount, successCount, details, null);
    }

    public static SurveyConversionAlert error(int totalCount, int successCount, String error) {
        return new SurveyConversionAlert(totalCount, successCount, List.of(), error);
    }

    public record SurveyDetails(
        String url,

        // SUCCESS
        String title,
        Long surveyId,
        Long memberId,
        int questionCount,
        List<UnsupportedQuestion> unsupportedList,

        // FAIL
        String message
    ) {

        public static SurveyDetails success(String url, String title, Long surveyId, Long memberId, int questionCount, List<UnsupportedQuestion> unsupportedList) {
            return new SurveyDetails(url, title, surveyId, memberId, questionCount, unsupportedList, null);
        }

        public static SurveyDetails failure(String url, String message) {
            return new SurveyDetails(url, null, null, null, 0, List.of(), message);
        }

        public record UnsupportedQuestion(
            int order,
            String type,
            String reason
        ) { }
    }
}
