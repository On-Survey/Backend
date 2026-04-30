package OneQ.OnSurvey.domain.survey.model.export;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SurveyAnswerProjection {
    private final Long memberId;
    private final Long questionId;
    private final String content;
    // 그리드 이외 타입은 기본값 0을 가지도록 함.
    private final Integer gridRowOrder;
}
