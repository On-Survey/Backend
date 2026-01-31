package OneQ.OnSurvey.domain.admin.infra;

import OneQ.OnSurvey.global.common.exception.ApiErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AdminException implements ApiErrorCode {

    BO_UNAUTHORIZED("ADMIN_001", "어드민 인증 정보가 부족합니다.", HttpStatus.UNAUTHORIZED),
    BO_FORBIDDEN("ADMIN_002", "어드민 인증에 실패했습니다.", HttpStatus.FORBIDDEN),
    BO_REGISTER_VALIDATION("ADMIN_003", "어드민 등록에 필요한 데이터가 부족합니다..", HttpStatus.BAD_REQUEST),
    BO_REGISTER_FAILED("ADMIN_004", "어드민 등록에 실패했습니다.", HttpStatus.BAD_REQUEST);

    private final String errorCode;
    private final String message;
    private final HttpStatus status;
}
