package OneQ.OnSurvey.domain.question.model.dto.type;

import OneQ.OnSurvey.domain.question.entity.question.Choice;
import OneQ.OnSurvey.domain.question.model.dto.OptionDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter @SuperBuilder @ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChoiceDto extends DefaultQuestionDto {
    private Integer maxChoice;
    private Boolean hasNoneOption;
    private Boolean hasCustomInput;
    private Boolean isSectionDecidable;

    private List<OptionDto> options;

    public static ChoiceDto fromEntity(Choice choice) {
        return ChoiceDto.builder()
            .maxChoice(choice.getMaxChoice())
            .hasNoneOption(Boolean.TRUE.equals(choice.getHasNoneOption()))
            .hasCustomInput(Boolean.TRUE.equals(choice.getHasCustomInput()))
            .isSectionDecidable(Boolean.TRUE.equals(choice.getIsSectionDecidable()))
            .questionId(choice.getQuestionId())
            .surveyId(choice.getSurveyId())
            .questionType(choice.getType())
            .title(choice.getTitle())
            .description(choice.getDescription())
            .isRequired(choice.getIsRequired())
            .questionOrder(choice.getOrder())
            .section(choice.getSection() != null ? choice.getSection() : 1)
            .build();
    }

    public void updateOptions(List<OptionDto> optionInfoList) {
        this.options = optionInfoList;
    }
}
