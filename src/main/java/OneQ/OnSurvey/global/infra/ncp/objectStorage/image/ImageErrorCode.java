package OneQ.OnSurvey.global.infra.ncp.objectStorage.image;

import OneQ.OnSurvey.global.exception.ApiErrorCode;
import org.springframework.http.HttpStatus;

public enum ImageErrorCode implements ApiErrorCode {

    NO_AUTHORITY("IMG403", "이미지 업로드 권한이 없습니다.", HttpStatus.FORBIDDEN),
    INVALID_TYPE("IMG415", "지원하지 않는 이미지 형식입니다.", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    TOO_LARGE("IMG413", "이미지 최대 용량(10MB)을 초과했습니다.", HttpStatus.PAYLOAD_TOO_LARGE),
    UPLOAD_FAILED("IMG500", "이미지 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    IMAGE_NOT_FOUND("POST404", "해당 이미지를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NOT_PUBLIC("IMG409", "퍼블릭 접근이 불가합니다.", HttpStatus.CONFLICT);

    private final String errorCode;
    private final String message;
    private final HttpStatus status;

    ImageErrorCode(String code, String msg, HttpStatus status) {
        this.errorCode = code; this.message = msg; this.status = status;
    }
    @Override public String getErrorCode() { return errorCode; }
    @Override public String getMessage() { return message; }
    @Override public HttpStatus getStatus() { return status; }
}

