package OneQ.OnSurvey.global.infra.discord.notifier;

import OneQ.OnSurvey.global.infra.discord.DiscordAlarmAsyncFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
@RequiredArgsConstructor
public class DiscordErrorAlertNotifier implements ErrorAlertNotifier {

    private final DiscordAlarmAsyncFacade discord;

    @Override
    public void sendErrorAlertAsync(Exception e, String method, String uri, String queryString) {
        discord.sendErrorAlertAsync(e, method, uri, queryString);
    }
}
