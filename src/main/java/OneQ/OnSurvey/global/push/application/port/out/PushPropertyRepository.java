package OneQ.OnSurvey.global.push.application.port.out;

import java.util.Map;

public interface PushPropertyRepository {

    Map<String, String> findPushTemplateContextByCode(String code);
}
