package OneQ.OnSurvey.domain.survey.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public record FreeSurveyFormRequest(
        @Future
        @Schema(description = "설문 마감일", example = "2026-12-31T23:59:59")
        LocalDateTime deadline,
        @Schema(description = "설문 이미지 URL")
        @Pattern(regexp = "^(https?)://.+$", message = "imageUrl은 http/https URL 형식이어야 합니다.")
        String imageUrl
) {}
