package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.survey.entity.Survey;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SurveyFormResponse (
    Long surveyId,
    String title,
    String description,
    Integer totalCoin,
    LocalDateTime createdAt,
    String imageUrl
) {

    public static SurveyFormResponse fromEntity(Survey survey) {
        return SurveyFormResponse.builder()
            .surveyId(survey.getId())
            .title(survey.getTitle())
            .description(survey.getDescription())
            .totalCoin(survey.getTotalCoin())
            .createdAt(survey.getCreatedAt())
            .imageUrl(survey.getImageUrl())
            .build();
    }
}
