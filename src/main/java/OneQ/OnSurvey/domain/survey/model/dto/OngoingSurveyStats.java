package OneQ.OnSurvey.domain.survey.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OngoingSurveyStats {
    private Long surveyId;
    private String title;
    private Integer completedCount;
    private Integer dueCount;
}
