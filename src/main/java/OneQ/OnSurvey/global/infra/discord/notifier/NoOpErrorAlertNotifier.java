package OneQ.OnSurvey.global.infra.discord.notifier;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class NoOpErrorAlertNotifier implements ErrorAlertNotifier {

    @Override
    public void sendErrorAlertAsync(Exception e, String method, String uri, String queryString) {
    }
}
