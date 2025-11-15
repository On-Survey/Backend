package OneQ.OnSurvey.domain.survey.model.request;

import OneQ.OnSurvey.domain.member.value.Interest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.Set;

@Getter
public class SurveyInterestRequest {

    @Schema(
        description = "관심사 목록",
        example = "[\"CAREER\", \"BUSINESS\", \"FINANCE\"]",
        implementation = Interest.class
    )
    Set<Interest> interests;
}
