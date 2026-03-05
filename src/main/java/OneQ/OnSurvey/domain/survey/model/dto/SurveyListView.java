package OneQ.OnSurvey.domain.survey.model.dto;

import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class SurveyListView {
    private Long surveyId;
    private String title;
    private Long creator;
    private LocalDateTime createdAt;
    private SurveyStatus status;
}
