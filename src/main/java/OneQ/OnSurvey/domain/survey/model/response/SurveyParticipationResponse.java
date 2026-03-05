package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.survey.model.dto.SurveyWithEligibility;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter @Builder
public class SurveyParticipationResponse {
    @Schema(description = "전체 설문")
    private List<SurveyData> surveys;
    @Schema(description = "다음 설문 페이지 존재 여부")
    private Boolean hasNext;

    @Schema(description = "사용자 추천 설문")
    private List<SurveyData> recommended;
    @Schema(description = "마감 임박 설문")
    private List<SurveyData> impending;
    @Schema(description = "사용자 추천 설문 다음 문항 존재 여부")
    private Boolean recommendedHasNext;
    @Schema(description = "마감 임박 설문 다음 문항 존재 여부")
    private Boolean impendingHasNext;

    @Getter @Builder
    @AllArgsConstructor
    public static class SurveyData {
        private Long surveyId;
        private Long memberId;
        private String title;
        private String description;
        private Boolean isFree;
        private Set<Interest> interests;
        private LocalDateTime deadline;

        @Schema(description = "사용자 세그멘테이션 적격 여부")
        private Boolean isEligible;
    }

    public static SurveyData from(SurveyWithEligibility surveyWithEligibility) {
        return SurveyData.builder()
            .surveyId(surveyWithEligibility.getSurveyId())
            .memberId(surveyWithEligibility.getMemberId())
            .title(surveyWithEligibility.getTitle())
            .description(surveyWithEligibility.getDescription())
            .isFree(surveyWithEligibility.getIsFree())
            .interests(surveyWithEligibility.getInterests())
            .deadline(surveyWithEligibility.getDeadline())
            .isEligible(surveyWithEligibility.getIsEligible())
            .build();
    }

    @Getter @AllArgsConstructor
    public static class SliceSurveyData {
        private List<SurveyData> surveyDataList;
        private Boolean hasNext;
    }
}
