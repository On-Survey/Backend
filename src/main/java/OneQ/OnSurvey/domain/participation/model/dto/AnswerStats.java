package OneQ.OnSurvey.domain.participation.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class AnswerStats {
    Long questionId;
    String content;
    Integer count;

    public AnswerStats(Long questionId, String content) {
        this.questionId = questionId;
        this.content = content;
        this.count = 1;
    }
}
