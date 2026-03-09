package OneQ.OnSurvey.global.push.domain.vo;

import java.util.Map;

public record PushTemplateVO(
    String code,
    Map<String, String> context
) {
}
