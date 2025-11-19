package OneQ.OnSurvey.domain.survey.service.refund;

import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.entity.SurveyInfo;
import org.springframework.stereotype.Component;

@Component
public class DefaultSurveyRefundPolicy implements SurveyRefundPolicy {

    private static final int REWARD_PER_RESPONSE = 300;

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
        int paidReward = completedCount * REWARD_PER_RESPONSE;

        int refundBase = Math.max(totalCoin - paidReward, 0);

        int lackCount  = targetCount - completedCount;
        double lackRatio = (double) lackCount / targetCount;

        return (int) Math.floor(refundBase * lackRatio);
    }
}

