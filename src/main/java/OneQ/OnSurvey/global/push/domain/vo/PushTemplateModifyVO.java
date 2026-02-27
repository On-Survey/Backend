package OneQ.OnSurvey.global.push.domain.vo;

import OneQ.OnSurvey.global.infra.toss.common.dto.push.PushTemplateModifyRequest;

import java.util.List;

public record PushTemplateModifyVO(
    String code,
    List<ModifyPushTemplate> modifyTemplateList
) {

    public record ModifyPushTemplate(
        String contextKey,
        String contextValue,
        String description
    ) {}

    public static PushTemplateModifyVO of(PushTemplateModifyRequest pushTemplateModifyRequest) {
        return new PushTemplateModifyVO(
            pushTemplateModifyRequest.code(),
            pushTemplateModifyRequest.defaultContext().entrySet().stream().map(
                entry -> new ModifyPushTemplate(
                    entry.getKey(),
                    entry.getValue().get(0),
                    entry.getValue().get(1)
                )
            )
            .toList()
        );
    }
}
