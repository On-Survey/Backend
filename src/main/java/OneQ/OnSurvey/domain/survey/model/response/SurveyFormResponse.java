package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.survey.entity.Survey;
import lombok.Builder;

@Builder
public record SurveyFormResponse (
    Long surveyId,
    String title,
    String description
) {

    public static SurveyFormResponse fromEntity(Survey survey) {
        return SurveyFormResponse.builder()
            .surveyId(survey.getId())
            .title(survey.getTitle())
            .description(survey.getDescription())
            .build();
    }
}
