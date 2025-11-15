package OneQ.OnSurvey.domain.survey;

import OneQ.OnSurvey.global.exception.ApiErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SurveyErrorCode implements ApiErrorCode {

    SURVEY_NOT_FOUND("SURVEY_404", "설문을 찾지 못했습니다.", HttpStatus.NOT_FOUND),
    SURVEY_INFO_NOT_FOUND("SURVEY_INFO_404", "설문 정보를 찾지 못했습니다.", HttpStatus.NOT_FOUND);

    private final String errorCode;
    private final String message;
    private final HttpStatus status;
}
