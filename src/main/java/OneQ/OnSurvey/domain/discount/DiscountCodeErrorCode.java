package OneQ.OnSurvey.domain.discount;

import OneQ.OnSurvey.global.common.exception.ApiErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum DiscountCodeErrorCode implements ApiErrorCode {

    DISCOUNT_CODE_NOT_FOUND("DISCOUNT_404", "유효하지 않은 할인 코드입니다.", HttpStatus.NOT_FOUND),
    DISCOUNT_CODE_EXPIRED("DISCOUNT_410", "만료된 할인 코드입니다.", HttpStatus.GONE);

    private final String errorCode;
    private final String message;
    private final HttpStatus status;
}
