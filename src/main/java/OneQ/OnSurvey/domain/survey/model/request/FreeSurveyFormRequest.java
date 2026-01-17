package OneQ.OnSurvey.domain.survey.model.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record FreeSurveyFormRequest(
        @Schema(description = "설문 마감일", example = "2024-12-31T23:59:59")
        LocalDateTime deadline
) {}
