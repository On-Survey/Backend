package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.survey.entity.Screening;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter @Builder
public class SurveyParticipationScreeningResponse {
    private List<ScreeningData> data;

    @Schema(description = "다음 문항 존재 여부")
    private Boolean hasNext;

    @Getter @Builder
    @AllArgsConstructor
    public static class ScreeningData {
        private Long screeningId;
        private Long surveyId;
        private String content;
        private Boolean answer;
    }

    public static ScreeningData fromEntity(Screening screening) {
        return ScreeningData.builder()
            .screeningId(screening.getId())
            .surveyId(screening.getSurveyId())
            .content(screening.getContent())
            .answer(screening.getAnswer())
            .build();
    }
}
