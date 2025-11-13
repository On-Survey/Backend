package OneQ.OnSurvey.global.infra.toss.auth.dto;

public record TossLoginResponse(
        boolean onboardingCompleted
) {
    public static TossLoginResponse of(boolean onboardingCompleted) {
        return new TossLoginResponse(onboardingCompleted);
    }
}
