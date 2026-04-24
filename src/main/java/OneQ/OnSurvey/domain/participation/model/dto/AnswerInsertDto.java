package OneQ.OnSurvey.domain.participation.model.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter @Builder
public class AnswerInsertDto {
    private Integer section;
    private List<AnswerInfo> answerInfoList;

    @Getter @Builder
    public static class AnswerInfo {
        private Long id;
        private Long memberId;
        private Integer gridRowOrder;
        // TODO: 응답값을 List로 받도록 하여, 중복선택 응답 처리를 쉽도록 수정 (스크리닝 응답은 단일 원소의 리스트로 받도록)
        private String content;

        public Boolean getBooleanContent() {
            return "true".equalsIgnoreCase(content);
        }
    }
}
