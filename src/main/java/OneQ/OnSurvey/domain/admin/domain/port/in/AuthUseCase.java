package OneQ.OnSurvey.domain.admin.domain.port.in;

public interface AuthUseCase {

    String authenticate(String username, String rawPassword);

    boolean register(Long userKey, String username, String password, String name);
}
