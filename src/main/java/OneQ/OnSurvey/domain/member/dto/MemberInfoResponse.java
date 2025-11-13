package OneQ.OnSurvey.domain.member.dto;

public record MemberInfoResponse(
        String name,
        Long coin
) {
    public static MemberInfoResponse of(String name, Long coin) {
        return new MemberInfoResponse(name, coin);
    }
}