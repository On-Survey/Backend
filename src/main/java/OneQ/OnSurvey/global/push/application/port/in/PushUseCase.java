package OneQ.OnSurvey.global.push.application.port.in;

import OneQ.OnSurvey.global.infra.toss.common.dto.push.PushTemplateAddRequest;
import OneQ.OnSurvey.global.infra.toss.common.dto.push.PushTemplateModifyRequest;

import java.util.Map;

public interface PushUseCase {

    void fillTemplateAndSendPush(PushCommand command);

    void addPushTemplate(PushTemplateAddRequest request);

    void modifyTemplateContext(PushTemplateModifyRequest request);

    record PushCommand(
        long userKey,
        String templateName,
        Map<String, String> templateCtx
    ) {}
}
