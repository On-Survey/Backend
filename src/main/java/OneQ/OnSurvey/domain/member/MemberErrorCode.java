package OneQ.OnSurvey.domain.member;

import OneQ.OnSurvey.global.common.exception.ApiErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements ApiErrorCode {

    MEMBER_NOT_FOUND("MEMBER404", "해당 멤버를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String errorCode;
    private final String message;
    private final HttpStatus status;
}
