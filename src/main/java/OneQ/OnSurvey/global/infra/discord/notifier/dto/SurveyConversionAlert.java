package OneQ.OnSurvey.global.infra.discord.notifier.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record SurveyConversionAlert(
    long requestId,
    int totalCount,
    int successCount,
    List<ConversionDetails> details
) {
    public record ConversionDetails(
        String url,
        String status, // "SUCCESS", "FAIL"

        // SUCCESS
        String title,
        Long surveyId,
        int questionCount,
        // FAIL
        String message
    ) {

        public static ConversionDetails success(String url, String title, Long surveyId, int questionCount) {
            return new ConversionDetails(url, "SUCCESS", title, surveyId, questionCount, null);
        }

        public static ConversionDetails fail(String url, String message) {
            return new ConversionDetails(url, "FAIL", null, null, 0, message);
        }

        public boolean isSuccess() {
            return "SUCCESS".equals(status);
        }
    }
}
