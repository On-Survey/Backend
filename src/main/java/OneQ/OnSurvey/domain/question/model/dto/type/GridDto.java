package OneQ.OnSurvey.domain.question.model.dto.type;

import OneQ.OnSurvey.domain.question.entity.question.Grid;
import OneQ.OnSurvey.domain.question.model.dto.GridOptionDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter @SuperBuilder @ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GridDto extends DefaultQuestionDto {
    private Boolean isCheckbox;
    private Boolean isChoiceMixed;
    private Boolean isChoiceDistinct;

    private List<GridOptionDto> gridOptions;

    public static GridDto fromEntity(Grid grid) {
        return GridDto.builder()
            .isCheckbox(grid.getIsCheckbox())
            .isChoiceMixed(grid.getIsChoiceMixed())
            .isChoiceDistinct(grid.getIsChoiceDistinct())
            .questionId(grid.getQuestionId())
            .surveyId(grid.getSurveyId())
            .questionType(grid.getType())
            .title(grid.getTitle())
            .description(grid.getDescription())
            .isRequired(grid.getIsRequired())
            .questionOrder(grid.getOrder())
            .section(grid.getSection() != null ? grid.getSection() : 1)
            .imageUrl(grid.getImageUrl())
            .build();
    }

    public void updateGridOptions(List<GridOptionDto> gridOptions) {
        this.gridOptions = gridOptions;
    }
}
