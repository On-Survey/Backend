package OneQ.OnSurvey.domain.question.model.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter @Builder
public class OptionUpsertDto {
    private final Long questionId;
    private final List<UpsertInfo> upsertInfoList;

    @Getter @Builder
    public static class UpsertInfo {
        Long optionId;
        String content;
        Long nextQuestionId;
    }
}
