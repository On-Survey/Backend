package OneQ.OnSurvey.domain.survey.model.formRequest;

import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.survey.entity.FormRequest;
import OneQ.OnSurvey.domain.survey.model.request.ScreeningRequest;
import OneQ.OnSurvey.domain.survey.model.request.SurveyFormRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

import java.util.Set;

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
    SurveyFormRequest surveyForm,

    @Schema(
        description = "관심사 목록",
        example = "[\"CAREER\", \"BUSINESS\", \"FINANCE\"]",
        implementation = Interest.class
    )
    Set<Interest> interests
) {
    public FormRequest toEntity(long userKey) {
        return FormRequest.createRequest(formLink, requesterEmail, userKey);
    }
}
