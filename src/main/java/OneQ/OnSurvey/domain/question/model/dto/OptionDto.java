package OneQ.OnSurvey.domain.question.model.dto;

import OneQ.OnSurvey.domain.question.entity.ChoiceOption;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter @Builder @ToString
public class OptionDto {
    private Long optionId;
    private Long questionId;
    private String content;
    private Integer nextSection;
    private String imageUrl;

    public static OptionDto fromEntity(ChoiceOption choiceOption) {
        return OptionDto.builder()
            .optionId(choiceOption.getChoiceOptionId())
            .questionId(choiceOption.getQuestionId())
            .content(choiceOption.getContent())
            .nextSection(choiceOption.getNextSection())
            .imageUrl(choiceOption.getImageUrl())
            .build();
    }
}
