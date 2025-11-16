package OneQ.OnSurvey.domain.survey.model.request;

import OneQ.OnSurvey.domain.survey.model.AgeRange;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.Residence;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record SurveyFormRequest (
        @Schema(description = "설문 제목", example = "설문1")
        String title,
        @Schema(description = "설문 설명", example = "설문1에 대한 설명입니다.")
        String description,
        @Schema(description = "설문 마감일", example = "2024-12-31T23:59:59")
        LocalDateTime deadline,

        @Schema(description = "성별", example = "ALL")
        Gender gender,
        @Schema(description = "성별 가격", example = "100")
        Integer genderPrice,

        @Schema(description = "연령대", example = "TWENTY")
        AgeRange age,
        @Schema(description = "연령대 가격", example = "100")
        Integer agePrice,

        @Schema(description = "거주지", example = "SEOUL")
        Residence residence,
        @Schema(description = "거주지 가격", example = "100")
        Integer residencePrice,

        @Schema(description = "응답자 수", example = "50")
        Integer dueCount,
        @Schema(description = "응답자 수 가격", example = "5000")
        Integer dueCountPrice,

        @Schema(description = "총 코인", example = "10000")
        Integer totalCoin
) {
}
