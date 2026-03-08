package OneQ.OnSurvey.global.infra.discord;

import OneQ.OnSurvey.global.infra.discord.notifier.dto.PaymentCompletedAlert;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.SurveyConversionAlert;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.SurveySubmittedAlert;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.TossAccessTokenAlert;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.PushAlimAlert;
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

    @Async("discordAlarmExecutor")
    public void sendPaymentCompletedAsync(PaymentCompletedAlert alert) {
        service.sendPaymentCompletedAlert(alert);
    }

    @Async("discordAlarmExecutor")
    public void sendSurveySubmittedAsync(SurveySubmittedAlert alert) {
        service.sendSurveySubmittedAlert(alert);
    }

    @Async("discordAlarmExecutor")
    public void sendTossAccessTokenAsync(TossAccessTokenAlert alert) {
        service.sendTossAccessTokenAlert(alert);
    }
  
    @Async("discordAlarmExecutor")
    public void sendSurveyConversionAsync(SurveyConversionAlert alert) {
        service.sendSurveyConversionAlert(alert);
    }

    @Async("discordAlarmExecutor")
    public void sendPushAlimAsync(PushAlimAlert alert) {
        service.sendPushAlimAsync(alert);
}
