package OneQ.OnSurvey.domain.participation.model.event;

import OneQ.OnSurvey.global.common.event.pushAlim.PushAlimEvent;

import java.util.Map;

public record SurveyCompletedEvent (
    Long userKey,
    Map<String, String> eventContext
) implements PushAlimEvent {

    @Override
    public Long getTargetUserKey() {
        return userKey;
    }

    @Override
    public String getPushTemplateName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Map<String, String> getPushContext() {
        return eventContext;
    }
}
