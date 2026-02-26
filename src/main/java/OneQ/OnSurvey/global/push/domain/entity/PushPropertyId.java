package OneQ.OnSurvey.global.push.domain.entity;

import java.io.Serializable;

public record PushPropertyId(
    String templateSetCode,
    String context
) implements Serializable { }
