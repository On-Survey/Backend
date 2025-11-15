package OneQ.OnSurvey.domain.question.model.dto.type;

import OneQ.OnSurvey.domain.question.entity.ChoiceOption;
import OneQ.OnSurvey.domain.question.entity.question.Choice;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter @SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChoiceDto extends DefaultQuestionDto {
    private Integer maxChoice;
    private Boolean hasNoneOption;
    private Boolean hasCustomInput;

    private List<OptionInfo> options;

    @Getter @Builder
    public static class OptionInfo {
        private Long optionId;
        private String content;
        private Long nextQuestionId;
    }

    public static OptionInfo fromEntity(ChoiceOption option) {
        return OptionInfo.builder()
            .optionId(option.getChoiceOptionId())
            .content(option.getContent())
            .nextQuestionId(option.getNextQuestionId())
            .build();
    }

    public static ChoiceDto fromEntity(Choice choice) {
        return ChoiceDto.builder()
            .maxChoice(choice.getMaxChoice())
            .questionId(choice.getQuestionId())
            .surveyId(choice.getSurveyId())
            .questionType(choice.getType())
            .title(choice.getTitle())
            .description(choice.getDescription())
            .isRequired(choice.getIsRequired())
            .questionOrder(choice.getOrder())
            .build();
    }
}
