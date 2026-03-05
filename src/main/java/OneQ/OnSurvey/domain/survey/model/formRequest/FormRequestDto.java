package OneQ.OnSurvey.domain.survey.model.formRequest;

import OneQ.OnSurvey.domain.survey.entity.FormRequest;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record FormRequestDto(
        @Schema(description = "폼 링크", example = "https://docs.google.com/forms/d/e/1FAIpQLSfD.../viewform")
        String formLink,

        @Schema(description = "질문 수", example = "15")
        Integer questionCount,

        @Schema(description = "목표 응답 수", example = "100")
        Integer targetResponseCount,

        @Schema(description = "마감일", example = "2026-12-31")
        LocalDate deadline,

        @Schema(description = "신청자 이메일", example = "test@gmail.com")
        String requesterEmail,

        @Schema(description = "가격", example = "9900")
        Integer price
) {
    public FormRequest toEntity() {
        return FormRequest.createRequest(
                formLink,
                questionCount,
                targetResponseCount,
                deadline,
                requesterEmail,
                price
        );
    }
}
