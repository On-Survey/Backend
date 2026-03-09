package OneQ.OnSurvey.global.infra.toss.common.dto.push;

import lombok.Builder;

import java.util.Map;

@Builder
public record PushTemplateSendRequest (
    long userKey,
    String templateSetCode,
    Map<String, String> templateCtx
) {
}
