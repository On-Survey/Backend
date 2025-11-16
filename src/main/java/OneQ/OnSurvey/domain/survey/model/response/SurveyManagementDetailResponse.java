package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.question.model.QuestionType;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter @RequiredArgsConstructor
public class SurveyManagementDetailResponse {

    private final Long surveyId;
    private final Long memberId;
    private final SurveyStatus status;

    private Integer currentCount;

    private List<DetailInfo> detailInfoList;

    @Getter
    @RequiredArgsConstructor
    public static class DetailInfo {
        private final Long questionId;
        private final Integer order;
        private final QuestionType type;
        private final String title;
        private final String description;
        private final Boolean isRequired;

        @Setter
        // 객관식 (CHIOCE, RATING, NPS) 설문 필드
        private Map<String, Long> answerMap;

        @Setter
        // 주관식 (SHORT, LONG, DATE, NUMBER) 설문 필드
        private List<String> answerList;
    }

    public void updateCurrentCount(Integer currentCount) {
        this.currentCount = currentCount;
    }

    public void updateDetailInfoList(List<DetailInfo> detailInfoList) {
        this.detailInfoList = detailInfoList;
    }
}
