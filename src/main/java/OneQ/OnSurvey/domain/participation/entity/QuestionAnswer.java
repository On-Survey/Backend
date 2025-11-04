package OneQ.OnSurvey.domain.participation.entity;

import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter @SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity @Table(name = "question_answer")
public class QuestionAnswer extends AbstractAnswer {
    @Column(name = "question_id")
    private Long questionId;

    @Column(length = 512)
    private String content;

    public static QuestionAnswer of(
        Long questionId,
        Long memberId,
        String content
    ) {
        return QuestionAnswer.builder()
            .questionId(questionId)
            .memberId(memberId)
            .content(content)
            .build();
    }

    public static QuestionAnswer from(AnswerInsertDto.AnswerInfo answerInfo, Long memberId) {
        return QuestionAnswer.of(
            answerInfo.getId(),
            memberId,
            answerInfo.getContent()
        );
    }
}