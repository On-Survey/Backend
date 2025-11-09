package OneQ.OnSurvey.domain.question.model.dto.type;

import OneQ.OnSurvey.domain.question.entity.question.Choice;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter @SuperBuilder
public class ChoiceDto extends DefaultQuestionDto {
    private Integer maxChoice;

    public static ChoiceDto fromEntity(Choice choice) {
        return ChoiceDto.builder()
            .maxChoice(choice.getMaxChoice())
            .questionId(choice.getQuestionId())
            .surveyId(choice.getSurveyId())
            .type(choice.getClass().toString())
            .title(choice.getTitle())
            .description(choice.getDescription())
            .isRequired(choice.getIsRequired())
            .questionOrder(choice.getOrder())
            .build();
    }
}
