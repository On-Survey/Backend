package OneQ.OnSurvey.domain.survey.model.formRequest;

import io.swagger.v3.oas.annotations.media.Schema;

public record FormValidationRequestDto(
    @Schema(description = "폼 편집 링크", example = "https://docs.google.com/forms/d/1FAIpQLSfD.../edit")
    String formLink,
    @Schema(description = "신청자 이메일", example = "test@gmail.com")
    String requesterEmail
) {
}
