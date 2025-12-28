package OneQ.OnSurvey.global.infra.discord.notifier;

public interface ErrorAlertNotifier {
    void sendErrorAlertAsync(Exception e, String method, String uri, String queryString);
}
