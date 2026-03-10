package OneQ.OnSurvey.global.promotion.application;

import OneQ.OnSurvey.domain.survey.service.query.SurveyQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromotionTierResolver {

    @Value("${toss.api.promotion.amount}")
    private int promotionAmount;

    @Value("${toss.api.promotion.code}")
    private String promotionCode;

    @Value("${toss.api.promotion.amount500}")
    private int promotionAmount500;

    @Value("${toss.api.promotion.code500}")
    private String promotionCode500;

    private final SurveyQueryService surveyQueryService;

    public PromoTier resolveBysurveyId(long surveyId) {
        Integer storedAmount = surveyQueryService.getPromotionAmountBySurveyId(surveyId);
        int amount = (storedAmount != null) ? storedAmount : promotionAmount;
        String code = (amount == promotionAmount500) ? promotionCode500 : promotionCode;
        return new PromoTier(code, amount);
    }

    public PromoTier resolveByCode(String code) {
        if (promotionCode500.equals(code)) {
            return new PromoTier(promotionCode500, promotionAmount500);
        }
        return new PromoTier(promotionCode, promotionAmount);
    }
}
