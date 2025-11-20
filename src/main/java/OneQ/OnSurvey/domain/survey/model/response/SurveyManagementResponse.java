package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.entity.SurveyInfo;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter @AllArgsConstructor
public class SurveyManagementResponse {
    List<SurveyInformation> infoList;

    @Getter @Builder
    public static class SurveyInformation {
        private Long surveyId;
        private String title;
        private String description;
        private SurveyStatus status;

        private LocalDateTime deadLine;
        private Integer dueCount;

        private Integer currentCount;

        // 기본 생성 일자
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public void updateCurrentParticipationCount(Integer count) {
            this.currentCount = count;
        }
    }

    public static SurveyInformation fromEntity(Survey survey, SurveyInfo info) {
        return SurveyInformation.builder()
            .surveyId(survey.getId())
            .title(survey.getTitle())
            .description(survey.getDescription())
            .status(survey.getStatus())
            .deadLine(survey.getDeadline())
            .dueCount(info.getDueCount())
            .currentCount(info.getCompletedCount())
            .createdAt(survey.getCreatedAt())
            .updatedAt(survey.getUpdatedAt())
            .build();
    }
}
