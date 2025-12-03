package OneQ.OnSurvey.global.infra.toss.common.exception;

import lombok.Getter;

@Getter
public class TossApiException extends RuntimeException {
    private final int code;
    private final String raw;

    public TossApiException(int code, String message, String raw) {
        super(message);
        this.code = code;
        this.raw = raw;
    }
}
