package OneQ.OnSurvey.global.auth.dto;

/**
 * 어드민 등록 요청 DTO
 */
public record AdminRegisterRequest(
    Long userKey,
    String username,
    String password,
    String name
) {
    public boolean validate() {
        return userKey != null
            && username != null && !username.isBlank()
            && password != null && !password.isBlank()
            && name != null && !name.isBlank();
    }
}
