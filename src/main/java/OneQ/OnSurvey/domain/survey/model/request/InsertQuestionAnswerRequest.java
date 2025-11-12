package OneQ.OnSurvey.domain.survey.model.request;

import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter @Builder
public class InsertQuestionAnswerRequest {
    private List<QuestionAnswerInfo> infoList;

    @Getter @AllArgsConstructor
    public static class QuestionAnswerInfo {
        private Long questionId;
        private String content;
    }

    public AnswerInsertDto toDto(Long memberId) {
        return AnswerInsertDto.builder()
            .answerInfoList(infoList.stream().map(info ->
                AnswerInsertDto.AnswerInfo.builder()
                    .id(info.getQuestionId())
                    .memberId(memberId)
                    .content(info.getContent())
                    .build())
                .toList())
            .build();
    }
}
