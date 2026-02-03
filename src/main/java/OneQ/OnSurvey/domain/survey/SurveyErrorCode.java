package OneQ.OnSurvey.domain.survey;

import OneQ.OnSurvey.global.common.exception.ApiErrorCode;
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
    SURVEY_WRONG_SEGMENTATION("SURVEY_PARTICIPATION_SEGMENTATION_403", "설문과 참여자의 세분화 정보가 일치하지 않습니다.", HttpStatus.FORBIDDEN),

    SURVEY_PARTICIPATION_TEMP_EXCEEDED("SURVEY_PARTICIPATION_TEMP_EXCEEDED_409", "설문 참여 가능 인원이 일시적으로 초과되었습니다.", HttpStatus.CONFLICT),
    SURVEY_PARTICIPATION_OWN_SURVEY("SURVEY_PARTICIPATION_OWN_403", "본인이 생성한 설문에는 참여할 수 없습니다.", HttpStatus.FORBIDDEN),

    SURVEY_INCORRECT_STATUS("SURVEY_STATUS_400", "요청과 설문 상태가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    SURVEY_FORM_INVALID_QUESTION_TYPE("SURVEY_FORM_QUESTION_TYPE_400", "문항 타입이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    SURVEY_FORM_EMPTY_REQUEST("SURVEY_FORM_400", "설문 요청이 비어있습니다.", HttpStatus.BAD_REQUEST),
    SURVEY_FORM_DUPLICATE_POST("SURVEY_FROM_DUPLICATE_400", "중복된 문항 생성 요청입니다.", HttpStatus.BAD_REQUEST),
    SURVEY_FORM_INVALID_SECTION("SURVEY_FORM_SECTION_400", "유효하지 않은 섹션 정보입니다.", HttpStatus.BAD_REQUEST),

    SURVEY_FORBIDDEN("SURVEY_403", "설문에 대한 권한이 없습니다.", HttpStatus.FORBIDDEN),

    SURVEY_ANSWER_INVALID("SURVEY_ANSWER_400", "설문 답변이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    SURVEY_FREE_PROMOTION_NOT_ALLOWED("SURVEY_PROMOTION_400", "무료 설문은 프로모션 지급 대상이 아닙니다.", HttpStatus.BAD_REQUEST),

    FORM_REQUEST_NOT_FOUND("FORM_REQUEST_404", "구글 폼 신청을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String errorCode;
    private final String message;
    private final HttpStatus status;
}