package OneQ.OnSurvey.global.infra.toss;

import OneQ.OnSurvey.global.exception.ApiErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TossErrorCode implements ApiErrorCode {

    TOSS_ACCESS_TOKEN_ERROR("TOSS_001", "TOSS 토큰 발행 중 에러가 발생했습니다.", HttpStatus.BAD_REQUEST),
    TOSS_GET_USER_INFO_ERROR("TOSS_002", "TOSS 유저 정보 가져오기 중 에러가 발생했습니다.", HttpStatus.BAD_REQUEST),
    INVALID_REFERRER("TOSS_WITHDRAWAL_001", "유효하지 않은 referrer입니다.", HttpStatus.BAD_REQUEST),
    TOSS_API_CONNECTION_ERROR("TOSS_500", "그 밖에 토스 api를 연동하는 과정에서 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String errorCode;
    private final String message;
    private final HttpStatus status;
}
