package OneQ.OnSurvey.domain.survey.model.formRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record FormValidationRequestDto(
    @Schema(description = "폼 편집 링크", example = "https://docs.google.com/forms/d/1FAIpQLSfD.../edit")
    @URL @NotBlank
    String formLink,

    @Schema(description = "신청자 이메일", example = "test@gmail.com")
    @Email @NotBlank
    String requesterEmail,

    @Schema(description = "유효성 검사 결과 이메일 수신 요청 여부")
    Boolean isEmailRequired
) {
}
