package OneQ.OnSurvey.domain.survey.service.refund;

import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.entity.SurveyInfo;
import OneQ.OnSurvey.domain.survey.model.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultSurveyRefundPolicyTest {

    private DefaultSurveyRefundPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new DefaultSurveyRefundPolicy();
        ReflectionTestUtils.setField(policy, "rewardPerResponse", 100);
    }

    private Survey surveyWithCoin(int totalCoin) {
        return Survey.builder()
                .memberId(1L)
                .title("테스트 설문")
                .description("설명")
                .totalCoin(totalCoin)
                .build();
    }

    private SurveyInfo surveyInfo(int dueCount, int completedCount) {
        return SurveyInfo.builder()
                .surveyId(1L)
                .dueCount(dueCount)
                .completedCount(completedCount)
                .gender(Gender.ALL)
                .ages(Set.of())
                .residences(Set.of())
                .genderPrice(0)
                .agePrice(0)
                .residencePrice(0)
                .dueCountPrice(0)
                .promotionAmount(0)
                .build();
    }

    @Test
    @DisplayName("목표 응답 수가 0 이하이면 환불 금액은 0")
    void calculateRefundAmount_whenTargetCountZero_returnsZero() {
        Survey survey = surveyWithCoin(1000);
        SurveyInfo info = surveyInfo(0, 0);

        int result = policy.calculateRefundAmount(survey, info);

        assertThat(result).isZero();
    }

    @Test
    @DisplayName("목표 응답 수가 음수이면 환불 금액은 0")
    void calculateRefundAmount_whenTargetCountNegative_returnsZero() {
        Survey survey = surveyWithCoin(1000);
        SurveyInfo info = surveyInfo(-1, 0);

        int result = policy.calculateRefundAmount(survey, info);

        assertThat(result).isZero();
    }

    @Test
    @DisplayName("완료 수가 목표 수 이상이면 환불 금액은 0")
    void calculateRefundAmount_whenCompletedReachedTarget_returnsZero() {
        Survey survey = surveyWithCoin(1000);
        SurveyInfo info = surveyInfo(10, 10);

        int result = policy.calculateRefundAmount(survey, info);

        assertThat(result).isZero();
    }

    @Test
    @DisplayName("완료 수가 목표 수를 초과해도 환불 금액은 0")
    void calculateRefundAmount_whenCompletedExceedsTarget_returnsZero() {
        Survey survey = surveyWithCoin(1000);
        SurveyInfo info = surveyInfo(10, 15);

        int result = policy.calculateRefundAmount(survey, info);

        assertThat(result).isZero();
    }

    @Test
    @DisplayName("응답이 0건일 때 전액 환불")
    void calculateRefundAmount_whenNoCompletions_returnsFullRefund() {
        // totalCoin=1000, rewardPerResponse=100, dueCount=10, completedCount=0
        // paidReward=0, refundBase=1000, lackRatio=1.0 → refund=1000
        Survey survey = surveyWithCoin(1000);
        SurveyInfo info = surveyInfo(10, 0);

        int result = policy.calculateRefundAmount(survey, info);

        assertThat(result).isEqualTo(1000);
    }

    @Test
    @DisplayName("절반 응답 완료 시 남은 비율만큼 환불")
    void calculateRefundAmount_whenHalfCompleted_returnsHalfRefund() {
        // totalCoin=1000, rewardPerResponse=100, dueCount=10, completedCount=5
        // paidReward=500, refundBase=500, lackRatio=5/10=0.5 → refund=250
        Survey survey = surveyWithCoin(1000);
        SurveyInfo info = surveyInfo(10, 5);

        int result = policy.calculateRefundAmount(survey, info);

        assertThat(result).isEqualTo(250);
    }

    @Test
    @DisplayName("totalCoin이 paidReward 이하이면 refundBase=0 → 환불 금액 0")
    void calculateRefundAmount_whenTotalCoinBelowPaidReward_returnsZero() {
        // totalCoin=300, rewardPerResponse=100, completedCount=5 → paidReward=500 > totalCoin
        // refundBase=0 → refund=0
        Survey survey = surveyWithCoin(300);
        SurveyInfo info = surveyInfo(10, 5);

        int result = policy.calculateRefundAmount(survey, info);

        assertThat(result).isZero();
    }

    @Test
    @DisplayName("소수점 이하는 내림 처리")
    void calculateRefundAmount_floorsFractionalAmount() {
        // totalCoin=1000, rewardPerResponse=100, dueCount=3, completedCount=1
        // paidReward=100, refundBase=900, lackRatio=2/3≈0.6667 → floor(600)=600
        Survey survey = surveyWithCoin(1000);
        SurveyInfo info = surveyInfo(3, 1);

        int result = policy.calculateRefundAmount(survey, info);

        assertThat(result).isEqualTo(600);
    }

    @Test
    @DisplayName("목표 1건 중 0건 완료 → 전액 환불")
    void calculateRefundAmount_singleTargetNoCompletion_fullRefund() {
        Survey survey = surveyWithCoin(500);
        SurveyInfo info = surveyInfo(1, 0);

        int result = policy.calculateRefundAmount(survey, info);

        assertThat(result).isEqualTo(500);
    }
}
