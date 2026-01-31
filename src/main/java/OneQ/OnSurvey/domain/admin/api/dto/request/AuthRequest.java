package OneQ.OnSurvey.domain.admin.api.dto.request;

public record AuthRequest(
    String username,
    String password
) {

    public boolean validate() {
        return username != null && !username.isBlank()
            && password != null;
    }
}
