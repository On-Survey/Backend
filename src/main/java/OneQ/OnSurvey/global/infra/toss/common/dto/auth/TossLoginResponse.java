package OneQ.OnSurvey.global.infra.toss.common.dto.auth;

public record TossLoginResponse(
        boolean onboardingCompleted
) {
    public static TossLoginResponse of(boolean onboardingCompleted) {
        return new TossLoginResponse(onboardingCompleted);
    }
}
