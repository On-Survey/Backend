package OneQ.OnSurvey.domain.survey.model.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record SurveyFormCreateRequest(
        @Schema(description = "설문 제목", example = "설문1")
        String title,
        @Schema(description = "설문 설명", example = "설문1에 대한 설명입니다.")
        String description
) {
}
