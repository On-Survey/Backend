package OneQ.OnSurvey.global.common.exception;

import org.springframework.http.HttpStatus;

public interface ApiErrorCode {
    String getErrorCode();
    String getMessage();
    HttpStatus getStatus();
}
