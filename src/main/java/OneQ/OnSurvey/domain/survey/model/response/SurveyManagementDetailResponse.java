package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.question.model.QuestionType;
import OneQ.OnSurvey.domain.survey.entity.SurveyInfo;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
    private SurveyInfoResponse surveyInfo;

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
        private final Integer section;

        @Setter
        @Schema(
            description = "(객관식, 평가형, NPS) 선택값 별 응답 집계",
            example = """ 
                {
                    "string": "number",
                    "answer1": 15,
                    "answer2": 10,
                    "answer3": 16
                }
            """
        )
        // 객관식 (CHOICE, RATING, NPS) 설문 필드
        private Map<String, Long> answerMap;

        @Setter
        @Schema(
            description = "(주관식) 응답값",
            example = """
                [
                    "string",
                    "answer1",
                    "answer2",
                    "answer3",
                    "answer4"
                ]
            """
        )
        // 주관식 (SHORT, LONG, DATE, NUMBER) 설문 필드
        private List<String> answerList;
    }

    public void updateCurrentCount(Integer currentCount) {
        this.currentCount = currentCount;
    }

    public void updateDetailInfoList(List<DetailInfo> detailInfoList) {
        this.detailInfoList = detailInfoList;
    }

    public void updateSurveyInfo(SurveyInfo info) {
        this.surveyInfo = SurveyInfoResponse.from(info);
    }
}
