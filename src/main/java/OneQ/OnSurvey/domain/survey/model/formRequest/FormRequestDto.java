package OneQ.OnSurvey.domain.survey.model.formRequest;

import OneQ.OnSurvey.domain.survey.entity.FormRequest;
import io.swagger.v3.oas.annotations.media.Schema;

public record FormRequestDto(
        @Schema(description = "폼 링크", example = "https://docs.google.com/forms/d/e/1FAIpQLSfD.../viewform")
        String formLink,

        @Schema(description = "신청자 이메일", example = "test@gmail.com")
        String requesterEmail
) {
    public FormRequest toEntity() {
        return FormRequest.createRequest(formLink, requesterEmail);
    }
}
