package OneQ.OnSurvey.domain.admin.domain.model.member;

public record AdminMemberView (
    Long id,
    Long userKey,
    String name,
    String email,
    String phoneNumber,
    String birthDay,
    String gender,
    String status,
    Long coin
) {
}
