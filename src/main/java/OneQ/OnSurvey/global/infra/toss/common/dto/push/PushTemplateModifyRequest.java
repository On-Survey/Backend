package OneQ.OnSurvey.global.infra.toss.common.dto.push;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

public record PushTemplateModifyRequest(
    @Schema(description = "발송할 메시지 템플릿 코드", example = "test_01")
    String code,

    @Schema(
        description = "템플릿에서 사용할 기본 context 값. key는 템플릿에서 사용할 변수명, value는 해당 변수에 들어갈 기본값(index 0)과 설명(index 1)을 담은 리스트",
        example = "{" +
            "\"surveyTitle\": [\"기본 설문 제목\", \"설문 제목에 들어갈 기본 변수값\"], " +
            "\"surveyDeadline\": [\"2024-12-31\", null]" +
            "}"
    )
    Map<String, List<String>> defaultContext
) {
}
