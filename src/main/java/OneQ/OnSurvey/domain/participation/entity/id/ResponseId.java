package OneQ.OnSurvey.domain.participation.entity.id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

@Getter @Builder
@EqualsAndHashCode
@AllArgsConstructor
public class ResponseId implements Serializable {
    private Long surveyId;
    private Long memberId;
}
