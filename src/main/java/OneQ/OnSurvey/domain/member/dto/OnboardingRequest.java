package OneQ.OnSurvey.domain.member.dto;

import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.survey.model.Residence;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

public record OnboardingRequest(
        @Schema(description = "거주지 정보", example = "SEOUL")
        Residence residence,

        @Schema(
                description = "관심사 목록",
                example = "[\"CAREER\", \"BUSINESS\", \"FINANCE\"]"
        )
        Set<Interest> interests
) {
}
