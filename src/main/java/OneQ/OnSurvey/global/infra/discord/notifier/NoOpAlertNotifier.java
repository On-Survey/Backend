package OneQ.OnSurvey.global.infra.discord.notifier;

import OneQ.OnSurvey.global.infra.discord.notifier.dto.PaymentCompletedAlert;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.PushAlimAlert;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.SurveyConversionAlert;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.SurveyHelpRequestAlert;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.SurveySubmittedAlert;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.TossAccessTokenAlert;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"local", "dev"})
public class NoOpAlertNotifier implements AlertNotifier {

    @Override
    public void sendErrorAlertAsync(Exception e, String method, String uri, String queryString) {}

    @Override
    public void sendPaymentCompletedAsync(PaymentCompletedAlert alert) {}

    @Override
    public void sendSurveySubmittedAsync(SurveySubmittedAlert alert) {}

    @Override
    public void sendTossAccessTokenAsync(TossAccessTokenAlert alert) {}

    @Override
    public void sendSurveyConversionAsync(SurveyConversionAlert alert) {}  
  
    @Override
    public void sendPushAlimAsync(PushAlimAlert alert) {}

    @Override
    public void sendSurveyHelpRequestAsync(SurveyHelpRequestAlert alert) {}
}
