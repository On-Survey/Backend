package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.survey.entity.Survey;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter @Builder
public class SurveyParticipationResponse {
    @Schema(description = "사용자 추천 설문")
    private List<SurveyData> recommended;
    @Schema(description = "마감 임박 설문")
    private List<SurveyData> impending;
    @Schema(description = "다음 문항 존재 여부")
    private Boolean hasNext;

    @Getter @Builder
    @AllArgsConstructor
    public static class SurveyData {
        private Long surveyId;
        private Long memberId;
        private String title;
        private String description;

        private Set<Interest> interests;

        private LocalDateTime deadline;
    }

    public static SurveyData fromEntity(Survey survey) {
        return SurveyData.builder()
            .surveyId(survey.getId())
            .memberId(survey.getMemberId())
            .title(survey.getTitle())
            .description(survey.getDescription())
            .interests(survey.getInterests())
            .deadline(survey.getDeadline())
            .build();
    }
}
