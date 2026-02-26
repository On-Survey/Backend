package OneQ.OnSurvey.global.push.application.port.in;

public interface PushUseCase {

    boolean fillTemplateAndSendPush(long userKey, String code, Map<String, String> templateContext);
}
