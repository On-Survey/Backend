package OneQ.OnSurvey.domain.survey.model.formRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;

public record FormValidationRequestDto(
    @Schema(description = "폼 편집 링크", example = "https://docs.google.com/forms/d/1FAIpQLSfD.../edit")
    @Pattern(
        regexp = "^https://docs\\.google\\.com/forms/d/[a-zA-Z0-9_-]+/(edit)(\\?.*)?$",
        message = "유효한 구글 폼 주소가 아닙니다. (.../edit 형태여야 합니다.)"
    )
    @URL @NotBlank
    String formLink,

    @Schema(description = "신청자 이메일", example = "test@gmail.com")
    @Email @NotBlank
    String requesterEmail,

    @Schema(description = "유효성 검사 결과 이메일 수신 요청 여부")
    Boolean isEmailRequired
) {
}
