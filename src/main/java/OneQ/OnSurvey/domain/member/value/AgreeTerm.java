package OneQ.OnSurvey.domain.member.value;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AgreeTerm {
    SERVICE_AGREED("serviceAgreed"),
    MARKETING_AGREED("marketingAgreed");

    private final String termName;
}
