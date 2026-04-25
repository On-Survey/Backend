package OneQ.OnSurvey.domain.question.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter @Builder @ToString
public class GridOptionUpsertDto {
    private Long questionId;
    private List<GridOptionDto> gridOptionInfoList;
}
