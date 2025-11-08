package OneQ.OnSurvey.global.infra.toss.common;

import OneQ.OnSurvey.global.exception.ApiErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TossErrorCode implements ApiErrorCode {

    TOSS_ACCESS_TOKEN_ERROR("TOSS_001", "TOSS 토큰 발행 중 에러가 발생했습니다.", HttpStatus.BAD_REQUEST),
    TOSS_GET_USER_INFO_ERROR("TOSS_002", "TOSS 유저 정보 가져오기 중 에러가 발생했습니다.", HttpStatus.BAD_REQUEST),
    TOSS_DECRYPT_ERROR("TOSS_003", "유저 정보 복호화에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REFERRER("TOSS_WITHDRAWAL_001", "유효하지 않은 referrer 입니다.", HttpStatus.BAD_REQUEST),

    TOSS_PROMOTION_NOT_FOUND("TOSS_PROMOTION_404", "토스 프로모션 지급 내역을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    TOSS_PROMOTION_API_ERROR("TOSS_PROMOTION_001", "토스 프로모션 지급 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    TOSS_PROMOTION_RETRYABLE("TOSS_PROMOTION_4110", "토스 일시 오류입니다. 잠시 후 다시 시도해주세요.", HttpStatus.SERVICE_UNAVAILABLE),
    TOSS_PROMOTION_DUPLICATE_KEY("TOSS_PROMOTION_4113", "이미 처리된 요청입니다.", HttpStatus.OK),
    TOSS_PROMOTION_BUDGET_EXHAUSTED("TOSS_PROMOTION_4109", "프로모션 예산이 소진되었습니다.", HttpStatus.BAD_REQUEST),

    TOSS_SSL_CONTEXT_BUILD_ERROR("TOSS_IAP_000", "토스 mTLS 초기화에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    TOSS_IAP_GET_STATUS_ERROR("TOSS_IAP_001", "IAP 주문 상태 조회에 실패했습니다.", HttpStatus.BAD_REQUEST),
    TOSS_IAP_INVALID_STATUS("TOSS_IAP_002", "IAP 주문 상태가 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    TOSS_IAP_STATUS_NOT_GRANTED("TOSS_IAP_003", "지급 가능한 주문 상태가 아닙니다.", HttpStatus.PRECONDITION_FAILED),
    TOSS_PARTNER_PRODUCT_GRANT_FAILED("TOSS_IAP_004", "상품 지급 처리에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    TOSS_API_CONNECTION_ERROR("TOSS_500", "그 밖에 토스 api를 연동하는 과정에서 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String errorCode;
    private final String message;
    private final HttpStatus status;
}
