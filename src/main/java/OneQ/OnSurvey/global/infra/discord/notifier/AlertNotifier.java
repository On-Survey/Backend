package OneQ.OnSurvey.global.infra.discord.notifier;

import OneQ.OnSurvey.global.infra.discord.notifier.dto.PaymentCompletedAlert;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.SurveySubmittedAlert;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.TossAccessTokenAlert;

public interface AlertNotifier {
    void sendErrorAlertAsync(Exception e, String method, String uri, String queryString);
    void sendPaymentCompletedAsync(PaymentCompletedAlert alert);
    void sendSurveySubmittedAsync(SurveySubmittedAlert alert);
    void sendTossAccessTokenAsync(TossAccessTokenAlert alert);
}
