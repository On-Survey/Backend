package OneQ.OnSurvey.global.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ApiErrorCode errorCode;

    public CustomException(ApiErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
