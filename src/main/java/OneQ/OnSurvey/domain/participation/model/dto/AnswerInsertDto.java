package OneQ.OnSurvey.domain.participation.model.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter @Builder
public class AnswerInsertDto {
    List<AnswerInfo> answerInfoList;

    @Getter @Builder
    public static class AnswerInfo {
        private Long id;
        private Long memberId;
        private String content;

        public Boolean getBooleanContent() {
            return "true".equalsIgnoreCase(content);
        }
    }
}
