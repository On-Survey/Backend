package OneQ.OnSurvey.domain.question.model;

import OneQ.OnSurvey.global.exception.CustomException;
import OneQ.OnSurvey.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum QuestionType {
    CHOICE("객관식", Values.CHOICE),
    RATING("평가식", Values.RATING),
    NPS("NPS", Values.NPS),
    SHORT("단답형", Values.SHORT),
    LONG("장문형", Values.LONG),
    NUMBER("숫자형", Values.NUMBER),
    DATE("날짜형", Values.DATE),
    TEXT("주관식", Values.TEXT);
    private final String value;
    private final String name;

    public static class Values {
        public static final String CHOICE = "CHOICE";
        public static final String RATING = "RATING";
        public static final String NPS = "NPS";
        public static final String SHORT = "SHORT_ANSWER";
        public static final String LONG = "LONG_ANSWER";
        public static final String NUMBER = "NUMBER";
        public static final String DATE = "DATE";
        public static final String TEXT = "TEXT";
    }

    public static QuestionType fromKey(String key) {
        return Arrays.stream(values())
            .filter(type -> Objects.equals(type.name, key))
            .findFirst()
            .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));
    }
}
