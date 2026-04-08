package OneQ.OnSurvey.global.infra.discord.notifier;

import OneQ.OnSurvey.global.infra.discord.DiscordAlarmAsyncFacade;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.PaymentCompletedAlert;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.SurveyConversionAlert;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.SurveyHelpRequestAlert;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.SurveySubmittedAlert;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.TossAccessTokenAlert;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.PushAlimAlert;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
@RequiredArgsConstructor
public class DiscordAlertNotifier implements AlertNotifier {

    private final DiscordAlarmAsyncFacade discord;

    @Override
    public void sendErrorAlertAsync(Exception e, String method, String uri, String queryString) {
        discord.sendErrorAlertAsync(e, method, uri, queryString);
    }

    @Override
    public void sendPaymentCompletedAsync(PaymentCompletedAlert alert) {
        discord.sendPaymentCompletedAsync(alert);
    }

    @Override
    public void sendSurveySubmittedAsync(SurveySubmittedAlert alert) {
        discord.sendSurveySubmittedAsync(alert);
    }

    @Override
    public void sendTossAccessTokenAsync(TossAccessTokenAlert alert) {
        discord.sendTossAccessTokenAsync(alert);
    }

    @Override
    public void sendSurveyConversionAsync(SurveyConversionAlert alert) {
        discord.sendSurveyConversionAsync(alert);
    }
  
    @Override
    public void sendPushAlimAsync(PushAlimAlert alert) {
        discord.sendPushAlimAsync(alert);
    }

    @Override
    public void sendSurveyHelpRequestAsync(SurveyHelpRequestAlert alert) {
        discord.sendSurveyHelpRequestAsync(alert);
    }
}
