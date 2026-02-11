package OneQ.OnSurvey.global.auth.dto;

/**
 * 어드민 로그인 결과 DTO
 */
public record AdminLoginResult(
    boolean success,
    String username
) {
    public static AdminLoginResult success(String username) {
        return new AdminLoginResult(true, username);
    }

    public static AdminLoginResult failure() {
        return new AdminLoginResult(false, null);
    }
}
