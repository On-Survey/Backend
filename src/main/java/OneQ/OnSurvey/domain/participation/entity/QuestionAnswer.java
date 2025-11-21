package OneQ.OnSurvey.domain.participation.entity;

import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity @Table(name = "question_answer")
public class QuestionAnswer extends AbstractAnswer {

    @Column(name = "question_id")
    private Long questionId;

    @Column(length = 512)
    private String content;

    @Builder
    private QuestionAnswer(Long questionId, Long memberId, String content) {
        this.questionId = questionId;
        this.memberId = memberId;
        this.content = content;
    }

    public static QuestionAnswer from(AnswerInsertDto.AnswerInfo info) {
        return QuestionAnswer.builder()
                .questionId(info.getId())
                .memberId(info.getMemberId())
                .content(info.getContent())
                .build();
    }
}