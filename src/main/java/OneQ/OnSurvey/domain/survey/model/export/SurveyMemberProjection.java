package OneQ.OnSurvey.domain.survey.model.export;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SurveyMemberProjection {
    private final Long memberId;
    private final String birthDay;
    private final String gender;
    private final String residence;
}
