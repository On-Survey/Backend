package OneQ.OnSurvey.global.payment;

import OneQ.OnSurvey.global.common.exception.ApiErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PaymentErrorCode implements ApiErrorCode {

    PAYMENT_PURPOSE_MISMATCH("PAYMENT400", "결제 목적이 일치하지 않습니다.", HttpStatus.BAD_REQUEST);

    private final String errorCode;
    private final String message;
    private final HttpStatus status;
}
