package OneQ.OnSurvey.domain.participation.entity.id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter @Builder
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class MemberSurveyStatusId {
    private Long surveyId;
    private Long memberId;
}
