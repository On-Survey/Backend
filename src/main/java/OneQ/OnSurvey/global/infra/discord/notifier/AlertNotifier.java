package OneQ.OnSurvey.global.infra.discord.notifier;

import OneQ.OnSurvey.global.infra.discord.notifier.dto.PaymentCompletedAlert;

public interface AlertNotifier {
    void sendErrorAlertAsync(Exception e, String method, String uri, String queryString);
    void sendPaymentCompletedAsync(PaymentCompletedAlert alert);
}
