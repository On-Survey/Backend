package OneQ.OnSurvey.domain.survey.model.formRequest;

import OneQ.OnSurvey.domain.survey.entity.FormRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record FormRequestDto(
        @Schema(description = "폼 링크", example = "https://docs.google.com/forms/d/e/1FAIpQLSfD.../viewform")
        @NotBlank
        String formLink,

        @Schema(description = "신청자 이메일", example = "test@gmail.com")
        @Email
        @NotBlank
        String requesterEmail
) {
    public FormRequest toEntity() {
        return FormRequest.createRequest(formLink, requesterEmail);
    }
}
