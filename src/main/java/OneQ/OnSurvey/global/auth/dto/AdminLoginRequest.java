package OneQ.OnSurvey.global.auth.dto;

/**
 * 어드민 로그인 요청 DTO
 */
public record AdminLoginRequest(
    String username,
    String password
) {
    public boolean validate() {
        return username != null && !username.isBlank()
            && password != null && !password.isBlank();
    }
}
