package OneQ.OnSurvey.global.infra.toss.common;

public class TossErrorMapper {

    public static TossErrorCode map(int tossCode) {
        return switch (tossCode) {
            case 4110 -> TossErrorCode.TOSS_PROMOTION_RETRYABLE;
            case 4113 -> TossErrorCode.TOSS_PROMOTION_DUPLICATE_KEY;
            case 4109, 4112 -> TossErrorCode.TOSS_PROMOTION_BUDGET_EXHAUSTED;
            default -> TossErrorCode.TOSS_PROMOTION_API_ERROR;
        };
    }
}
