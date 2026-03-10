package OneQ.OnSurvey.global.common.event.pushAlim;

import java.util.Map;

public interface PushAlimEvent {
    Long getTargetUserKey();
    String getPushTemplateName();
    Map<String, String> getPushContext();
}
