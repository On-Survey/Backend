package OneQ.OnSurvey.domain.survey;

import OneQ.OnSurvey.global.exception.ApiErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SurveyErrorCode implements ApiErrorCode {

    SURVEY_NOT_FOUND("SURVEY_404", "설문을 찾지 못했습니다.", HttpStatus.NOT_FOUND),
    SURVEY_INFO_NOT_FOUND("SURVEY_INFO_404", "설문 정보를 찾지 못했습니다.", HttpStatus.NOT_FOUND),
    SURVEY_NOT_REFUNDABLE("SURVEY_400", "해당 설문은 환불이 불가능합니다.", HttpStatus.BAD_REQUEST),
    SURVEY_ALREADY_PARTICIPATED("SURVEY_400", "이미 참여한 설문입니다.", HttpStatus.CONFLICT),

    SURVEY_INCORRECT_STATUS("SURVEY_STATUS_400", "요청과 설문 상태가 올바르지 않습니다.", HttpStatus.BAD_REQUEST);

    private final String errorCode;
    private final String message;
    private final HttpStatus status;
}
