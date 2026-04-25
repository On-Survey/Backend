package OneQ.OnSurvey.domain.question.model.dto;

import OneQ.OnSurvey.domain.question.entity.GridOption;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter @Builder @ToString
public class GridOptionDto {
    private Long gridOptionId;
    private Long questionId;
    private Boolean isRow;
    private String content;
    private Integer order;

    public static GridOptionDto fromEntity(GridOption gridOption) {
        return GridOptionDto.builder()
            .gridOptionId(gridOption.getGridOptionId())
            .questionId(gridOption.getQuestionId())
            .isRow(gridOption.getIsRow())
            .content(gridOption.getContent())
            .order(gridOption.getOrder())
            .build();
    }
}
