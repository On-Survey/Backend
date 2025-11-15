package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.member.value.Interest;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter @Builder
public class InterestResponse {
    private Set<Interest> interests;
}
