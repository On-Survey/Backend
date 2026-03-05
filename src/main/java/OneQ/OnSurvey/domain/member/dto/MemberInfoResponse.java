package OneQ.OnSurvey.domain.member.dto;

public record MemberInfoResponse(
        String name,
        String profileUrl,
        Long coin,
        Long promotionPoint,
        Boolean isOnboardingCompleted
) {
    public static MemberInfoResponse of(
        String name, String profileUrl, Long coin, Long promotionPoint, Boolean isOnboardingCompleted
    ) {
        return new MemberInfoResponse(name, profileUrl, coin, promotionPoint, isOnboardingCompleted);
    }
}