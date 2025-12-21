package OneQ.OnSurvey.domain.survey.model.export;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SurveyQuestionHeader {
    private final Long questionId;
    private final Integer orderNo;
    private final String title;
}
