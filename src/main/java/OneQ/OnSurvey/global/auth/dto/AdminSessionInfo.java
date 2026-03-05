package OneQ.OnSurvey.global.auth.dto;

/**
 * 어드민 세션 정보 DTO
 */
public record AdminSessionInfo(
    String username
) {
    public static AdminSessionInfo of(String username) {
        return new AdminSessionInfo(username);
    }
}
