package OneQ.OnSurvey.domain.survey.model.export;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
// TODO record 변경
public class SurveyAnswerProjection {
    private final Long memberId;
    private final Long questionId;
    private final String content;
}
