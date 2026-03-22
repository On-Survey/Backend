package OneQ.OnSurvey.domain.survey.model.request;

import OneQ.OnSurvey.domain.survey.model.AgeRange;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.Residence;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;

import java.time.LocalDateTime;
import java.util.List;

public record SurveyFormRequest(
        @Future
        @Schema(description = "설문 마감일", example = "2026-12-31T23:59:59")
        LocalDateTime deadline,

        @Schema(description = "성별", example = "ALL")
        Gender gender,

        @Schema(description = "연령대 목록", example = "[\"TEN\",\"TWENTY\",\"THIRTY\"]")
        List<AgeRange> ages,

        @Schema(description = "거주지", example = "SEOUL")
        Residence residence,

        @Schema(description = "응답자 수", example = "50")
        Integer dueCount,

        @Schema(description = "총 코인", example = "10000")
        Integer totalCoin,

        @Schema(description = "할인 코드 (선택)", example = "A1B2C3D4E5F6G7H8")
        String discountCode
) {
}