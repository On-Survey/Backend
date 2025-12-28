package OneQ.OnSurvey.global.infra.discord.notifier;

import OneQ.OnSurvey.global.infra.discord.notifier.dto.PaymentCompletedAlert;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class NoOpAlertNotifier implements AlertNotifier {

    @Override
    public void sendErrorAlertAsync(Exception e, String method, String uri, String queryString) {
    }

    @Override
    public void sendPaymentCompletedAsync(PaymentCompletedAlert alert) { }
}
