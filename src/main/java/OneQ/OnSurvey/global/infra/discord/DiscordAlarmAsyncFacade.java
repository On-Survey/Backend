package OneQ.OnSurvey.global.infra.discord;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiscordAlarmAsyncFacade {
    private final DiscordAlarmService service;

    @Async("discordAlarmExecutor")
    public void sendErrorAlertAsync(Throwable e, String method, String path, String query) {
        service.sendErrorAlert(e, method, path, query);
    }
}
