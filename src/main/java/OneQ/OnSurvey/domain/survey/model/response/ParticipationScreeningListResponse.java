package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.survey.model.dto.ScreeningIntroData;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter @Builder
public class ParticipationScreeningListResponse {
    private List<ScreeningIntroData> data;

    @Schema(description = "다음 문항 존재 여부")
    private Boolean hasNext;
}
