package OneQ.OnSurvey.domain.participation.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class AnswerStats {
    private Long questionId;
    private Integer gridRowOrder;
    private String content;
    private Long count;

    public AnswerStats(Long questionId, String content, Long count) {
        this.questionId = questionId;
        this.gridRowOrder = null;
        this.content = content;
        this.count = count;
    }

    public AnswerStats(Long questionId, String content) {
        this.questionId = questionId;
        this.gridRowOrder = null;
        this.content = content;
        this.count = 1L;
    }
}
