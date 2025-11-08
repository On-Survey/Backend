package OneQ.OnSurvey.domain.member;

import OneQ.OnSurvey.global.exception.ApiErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CoinErrorCode implements ApiErrorCode {

    COIN_NOT_POSITIVE("COIN400", "코인은 음수일 수 없습니다.", HttpStatus.NOT_FOUND),
    COIN_LACK("COIN401", "코인이 부족합니다.", HttpStatus.BAD_REQUEST);

    private final String errorCode;
    private final String message;
    private final HttpStatus status;
}
