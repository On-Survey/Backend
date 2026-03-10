package OneQ.OnSurvey.global.promotion.application;

import OneQ.OnSurvey.domain.survey.service.query.SurveyQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromotionTierResolver {

    @Value("${toss.api.promotion.amount500-question-min}")
    private int amount500QuestionMin;

    @Value("${toss.api.promotion.amount500-question-max}")
    private int amount500QuestionMax;

    @Value("${toss.api.promotion.amount}")
    private int promotionAmount;

    @Value("${toss.api.promotion.code}")
    private String promotionCode;

    @Value("${toss.api.promotion.amount500}")
    private int promotionAmount500;

    @Value("${toss.api.promotion.code500}")
    private String promotionCode500;

    private final SurveyQueryService surveyQueryService;

    /** SurveyInfo에 저장된 promotionAmount 기반으로 티어 결정 */
    public PromoTier resolveBySurveyId(long surveyId) {
        Integer storedAmount = surveyQueryService.getPromotionAmountBySurveyId(surveyId);
        int amount = (storedAmount != null) ? storedAmount : promotionAmount;
        String code = (amount == promotionAmount500) ? promotionCode500 : promotionCode;
        return new PromoTier(code, amount);
    }

    /** 저장된 promotionCode 기반으로 티어 역추적 (recheckPendingGrant용) */
    public PromoTier resolveByCode(String code) {
        if (promotionCode500.equals(code)) {
            return new PromoTier(promotionCode500, promotionAmount500);
        }
        return new PromoTier(promotionCode, promotionAmount);
    }

    /** 설문 제출 시 문항 수 기반으로 지급 금액 결정 */
    public int resolveAmountByQuestionCount(int questionCount) {
        if (questionCount >= amount500QuestionMin && questionCount <= amount500QuestionMax) {
            return promotionAmount500;
        }
        return promotionAmount;
    }
}
