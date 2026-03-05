package OneQ.OnSurvey.domain.question.model.dto.type;

import OneQ.OnSurvey.domain.question.model.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter @Builder
public class QuestionTypeAndInfoDto {
    @Schema(
        description = "문항 타입 유형",
        allowableValues = {
            "CHOICE", "RATING", "NPS", "SHORT", "LONG", "NUMBER", "DATE"
        }
    )
    private QuestionType questionType;
    private List<DefaultQuestionDto> questions;
}
