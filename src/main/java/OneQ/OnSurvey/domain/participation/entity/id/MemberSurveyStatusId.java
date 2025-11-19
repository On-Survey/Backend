package OneQ.OnSurvey.domain.participation.entity.id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class MemberSurveyStatusId {
    private Long surveyId;
    private Long memberId;
}
