package OneQ.OnSurvey.domain.question.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
    IMAGE("이미지형", Values.IMAGE),
    TITLE("제목형", Values.TITLE),
    GRID("그리드형", Values.GRID),
    TIME("시간형", Values.TIME);
    private final String description;
    private final String value;

    public static class Values {
        public static final String CHOICE = "CHOICE";
        public static final String RATING = "RATING";
        public static final String NPS = "NPS";
        public static final String SHORT = "SHORT";
        public static final String LONG = "LONG";
        public static final String NUMBER = "NUMBER";
        public static final String DATE = "DATE";
        public static final String IMAGE = "IMAGE";
        public static final String TITLE = "TITLE";
        public static final String GRID = "GRID";
        public static final String TIME = "TIME";
    }

    public boolean isText() {
        return SHORT.equals(this) || LONG.equals(this) || DATE.equals(this) || NUMBER.equals(this) || TIME.equals(this);
    }

    public boolean isChoice() {
        return CHOICE.equals(this);
    }

    public boolean isGrid() {
        return GRID.equals(this);
    }
}
