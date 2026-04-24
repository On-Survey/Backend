package OneQ.OnSurvey.domain.survey.model.request;

import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter @Builder
public class InsertQuestionAnswerRequest {
    @NotNull @Positive
    private Integer section;

    @Schema(
        description = "문항 응답 목록",
        example = """
            [
              { "questionId": 100, "rowOrder": null, "content": "옵션 A" },
              { "questionId": 101, "rowOrder": 0, "content": "매우 만족" },
              { "questionId": 101, "rowOrder": 0, "content": "매우 불만족" },
              { "questionId": 101, "rowOrder": 1, "content": "보통" }
            ]
            """
    )
    private List<QuestionAnswerInfo> infoList;

    @Getter @AllArgsConstructor
    @Schema(description = "개별 문항 응답 정보")
    public static class QuestionAnswerInfo {
        @Schema(description = "문항 ID", example = "202")
        @NotNull
        private Long questionId;

        @Schema(description = "그리드 행 순서(0부터 시작). GRID 문항이 아니면 null", example = "0", nullable = true)
        @PositiveOrZero
        private Integer rowOrder;

        @Schema(description = "응답 내용(선택 값/텍스트/그리드 열 값)", example = "매우 만족")
        private String content;
    }

    public AnswerInsertDto toDto(Long memberId) {
        infoList = (infoList != null) ? infoList : List.of();
        return AnswerInsertDto.builder()
            .section(section)
            .answerInfoList(infoList.stream().map(info ->
                AnswerInsertDto.AnswerInfo.builder()
                    .id(info.getQuestionId())
                    .memberId(memberId)
                    .gridRowOrder(info.getRowOrder())
                    .content(info.getContent())
                    .build())
                .toList())
            .build();
    }

    @JsonIgnore
    public boolean isEmpty() {
        return infoList == null || infoList.isEmpty();
    }
}
