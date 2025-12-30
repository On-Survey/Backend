package OneQ.OnSurvey.domain.survey.service.refund;

import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.entity.SurveyInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DefaultSurveyRefundPolicy implements SurveyRefundPolicy {

    @Value("${toss.api.promotion.amount}")
    private int rewardPerResponse;

    @Override
    public int calculateRefundAmount(Survey survey, SurveyInfo surveyInfo) {
        int targetCount    = surveyInfo.getDueCount();
        int completedCount = surveyInfo.getCompletedCount();

        if (targetCount <= 0) {
            return 0;
        }

        if (completedCount >= targetCount) {
            return 0;
        }

        int totalCoin  = survey.getTotalCoin();
        int paidReward = completedCount * rewardPerResponse;

        int refundBase = Math.max(totalCoin - paidReward, 0);

        int lackCount  = targetCount - completedCount;
        double lackRatio = (double) lackCount / targetCount;

        return (int) Math.floor(refundBase * lackRatio);
    }
}

