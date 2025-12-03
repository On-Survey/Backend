package OneQ.OnSurvey.global.auth;

import OneQ.OnSurvey.global.common.exception.ApiErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ApiErrorCode {

    INVALID_REFRESH_TOKEN("RT_401", "유효하지 않은 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED);

    private final String errorCode;
    private final String message;
    private final HttpStatus status;
}
