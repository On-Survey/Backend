package OneQ.OnSurvey.domain.question.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
    private final String description;
    private final String value;

    public static final Map<String, QuestionType> VALUE_MAP;

    static {
        Map<String, QuestionType> map = new HashMap<>();

        for (QuestionType type : values()) {
            map.put(type.getValue(), type);
        }

        VALUE_MAP = Collections.unmodifiableMap(map);
    }

    public static class Values {
        public static final String CHOICE = "CHOICE";
        public static final String RATING = "RATING";
        public static final String NPS = "NPS";
        public static final String SHORT = "SHORT";
        public static final String LONG = "LONG";
        public static final String NUMBER = "NUMBER";
        public static final String DATE = "DATE";
        public static final String TEXT = "TEXT";
    }

    public boolean isText() {
        return SHORT.equals(this) || LONG.equals(this) || DATE.equals(this) || TEXT.equals(this);
    }
}
