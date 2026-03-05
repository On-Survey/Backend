package OneQ.OnSurvey.global.push.domain.vo;

import OneQ.OnSurvey.global.infra.toss.common.dto.push.PushTemplateAddRequest;
import lombok.Builder;

import java.util.List;

@Builder
public record PushTemplateAddVO(
    List<NewPushTemplate> addTemplateList
) {

    public record NewPushTemplate(
        String name,
        String code,
        String contextKey,
        String contextValue,
        String description
    ) { }

    public static PushTemplateAddVO of(PushTemplateAddRequest pushTemplateAddRequest) {
        return pushTemplateAddRequest.defaultContext().isEmpty()
            ? new PushTemplateAddVO(List.of(
                new NewPushTemplate(
                    pushTemplateAddRequest.name(),
                    pushTemplateAddRequest.code(),
                    null,
                    null,
                    null
                )
            ))
            : new PushTemplateAddVO(
                pushTemplateAddRequest.defaultContext().entrySet().stream().map(
                    entry -> new NewPushTemplate(
                        pushTemplateAddRequest.name(),
                        pushTemplateAddRequest.code(),
                        entry.getKey(),
                        entry.getValue().getFirst(),
                        entry.getValue().size() >= 2 ? entry.getValue().get(1) : null
                    )
                )
                .toList()
            );
    }
}
