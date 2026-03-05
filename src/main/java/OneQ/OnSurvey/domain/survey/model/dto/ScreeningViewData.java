package OneQ.OnSurvey.domain.survey.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ScreeningViewData {
    private Long screeningId;
    private String content;
    private Boolean answer;
}
