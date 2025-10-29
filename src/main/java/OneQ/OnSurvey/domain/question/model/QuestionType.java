package OneQ.OnSurvey.domain.question.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QuestionType {
    CHOICE("객관식", Values.CHOICE),
    TEXT("주관식", Values.TEXT),
    RATING("평가식", Values.RATING),
    NPS("NPS", Values.NPS);

    private final String value;
    private final String name;

    public static class Values {
        public static final String CHOICE = "CHOICE";
        public static final String TEXT = "TEXT";
        public static final String RATING = "RATING";
        public static final String NPS = "NPS";
    }
}
