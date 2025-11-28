package OneQ.OnSurvey.domain.participation.entity;

import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity @Table(name = "screening_answer")
public class ScreeningAnswer extends AbstractAnswer {

    @Column(name = "screening_id")
    private Long screeningId;

    private Boolean content;

    public static ScreeningAnswer of(
            Long screeningId,
            Long memberId,
            Boolean content
    ) {
        ScreeningAnswer answer = new ScreeningAnswer();
        answer.screeningId = screeningId;
        answer.content = content;
        answer.setMemberId(memberId);
        return answer;
    }

    public static ScreeningAnswer from(AnswerInsertDto.AnswerInfo answerInfo) {
        return ScreeningAnswer.of(
                answerInfo.getId(),
                answerInfo.getMemberId(),
                answerInfo.getBooleanContent()
        );
    }
}