package OneQ.OnSurvey.global.exception;

import org.springframework.http.HttpStatus;

public interface ApiErrorCode {
    String getErrorCode();
    String getMessage();
    HttpStatus getStatus();
}
