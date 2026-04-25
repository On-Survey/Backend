package OneQ.OnSurvey.domain.participation.entity;

import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter @ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity @Table(name = "QUESTION_ANSWER")
public class QuestionAnswer extends AbstractAnswer {

    @Column(name = "QUESTION_ID", nullable = false)
    private Long questionId;

    @Column(name = "GRID_ROW_ORDER")
    private Integer gridRowOrder;

    @Column(length = 512, nullable = false)
    private String content;

    @Builder
    private QuestionAnswer(Long questionId, Long memberId, Integer gridRowOrder, String content) {
        this.questionId = questionId;
        this.memberId = memberId;
        this.gridRowOrder = gridRowOrder;
        this.content = content;
    }

    public static QuestionAnswer from(AnswerInsertDto.AnswerInfo info) {
        return QuestionAnswer.builder()
                .questionId(info.getId())
                .memberId(info.getMemberId())
                .gridRowOrder(info.getGridRowOrder())
                .content(info.getContent())
                .build();
    }

    public void updateContent(String content) {
        this.content = content;
    }
}