package OneQ.OnSurvey.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TokenExceptionMessage {

    INVALID_TOKEN("유효하지 않은 토큰입니다."),
    SESSION_EXPIRATION("만료된 세션입니다."),
    INVALID_HEADER("유효한 헤더가 아닙니다."),
    AUTH_ERROR("인증 중에 오류가 발생했습니다."),
    INVALID_AUTH("올바른 사용자가 아닙니다.");

    private final String message;
}
