package OneQ.OnSurvey.global.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ApiErrorCode errorCode;

    public CustomException(ApiErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
