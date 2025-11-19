package OneQ.OnSurvey.domain.question.model.dto;

import OneQ.OnSurvey.domain.question.entity.ChoiceOption;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter @Builder @ToString
public class OptionUpsertDto {
    private final Long questionId;
    private final List<OptionInfo> optionInfoList;

    @Getter @Builder @ToString
    public static class OptionInfo {
        Long optionId;
        String content;
        Long nextQuestionId;
    }

    public static OptionInfo fromEntity(ChoiceOption choiceOption) {
        return OptionInfo.builder()
            .optionId(choiceOption.getChoiceOptionId())
            .content(choiceOption.getContent())
            .nextQuestionId(choiceOption.getNextQuestionId())
            .build();
    }
}
