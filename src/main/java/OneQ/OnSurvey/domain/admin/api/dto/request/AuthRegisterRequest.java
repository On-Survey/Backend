package OneQ.OnSurvey.domain.admin.api.dto.request;

public record AuthRegisterRequest(
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
