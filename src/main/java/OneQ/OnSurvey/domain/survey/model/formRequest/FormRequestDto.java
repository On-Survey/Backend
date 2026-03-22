package OneQ.OnSurvey.domain.survey.model.formRequest;

import OneQ.OnSurvey.domain.survey.entity.FormRequest;
import OneQ.OnSurvey.domain.survey.model.request.ScreeningRequest;
import OneQ.OnSurvey.domain.survey.model.request.SurveyFormRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

public record FormRequestDto(
    @Schema(description = "폼 링크", example = "https://docs.google.com/forms/d/e/1FAIpQLSfD.../viewform")
    @URL @NotBlank
    String formLink,

    @Schema(description = "신청자 이메일", example = "test@gmail.com")
    @Email @NotBlank
    String requesterEmail,

    @Schema(description = "스크리닝 문항 (선택)")
    @Valid
    ScreeningRequest screening,

    @Schema(description = "세그먼트 및 가격 정보")
    @NotNull
    @Valid
    SurveyFormRequest surveyForm
) {
    public FormRequest toEntity(long userKey) {
        return FormRequest.createRequest(formLink, requesterEmail, userKey);
    }
}
