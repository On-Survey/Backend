package OneQ.OnSurvey.global.infra.ncp.objectStorage;

import OneQ.OnSurvey.global.common.exception.ApiErrorCode;
import org.springframework.http.HttpStatus;

public enum ObjectStorageErrorCode implements ApiErrorCode {

    BUCKET_NOT_CONFIGURED("OSS500", "Object Storage Bucket이 설정되지 않았습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_OBJECT_KEY   ("OSS400", "유효하지 않은 Object 키입니다.", HttpStatus.BAD_REQUEST),
    UPLOAD_FAILED        ("OSS500", "Object 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    HEAD_FAILED          ("OSS502", "Object 메타데이터 조회에 실패했습니다.", HttpStatus.BAD_GATEWAY),
    NOT_PUBLIC           ("OSS409", "Object가 Public으로 접근 불가합니다.", HttpStatus.CONFLICT),
    POLICY_APPLY_FAILED  ("OSS500", "Bucket Public 읽기 정책 적용에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    CORS_APPLY_FAILED    ("OSS500", "Bucket CORS 구성 적용에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String errorCode;
    private final String message;
    private final HttpStatus status;

    ObjectStorageErrorCode(String code, String msg, HttpStatus status) {
        this.errorCode = code; this.message = msg; this.status = status;
    }
    @Override public String getErrorCode() { return errorCode; }
    @Override public String getMessage()   { return message; }
    @Override public HttpStatus getStatus(){ return status; }
}
