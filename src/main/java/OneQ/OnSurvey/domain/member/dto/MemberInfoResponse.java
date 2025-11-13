package OneQ.OnSurvey.domain.member.dto;

public record MemberInfoResponse(
        String name,
        String profileUrl,
        Long coin
) {
    public static MemberInfoResponse of(String name, String profileUrl, Long coin) {
        return new MemberInfoResponse(name, profileUrl, coin);
    }
}