package OneQ.OnSurvey.global.infra.discord.notifier;

import OneQ.OnSurvey.global.infra.discord.DiscordAlarmAsyncFacade;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.PaymentCompletedAlert;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.SurveyConversionAlert;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.SurveySubmittedAlert;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.TossAccessTokenAlert;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Profile("local")
public class NoOpAlertNotifier implements AlertNotifier {

    private final DiscordAlarmAsyncFacade discord;

    @Override
    public void sendErrorAlertAsync(Exception e, String method, String uri, String queryString) {}

    @Override
    public void sendPaymentCompletedAsync(PaymentCompletedAlert alert) {}

    @Override
    public void sendSurveySubmittedAsync(SurveySubmittedAlert alert) {}

    @Override
    public void sendTossAccessTokenAsync(TossAccessTokenAlert alert) {}

    @Override
    public void sendSurveyConversionAsync(SurveyConversionAlert alert) {
        discord.sendSurveyConversionAsync(alert);
    }
}
