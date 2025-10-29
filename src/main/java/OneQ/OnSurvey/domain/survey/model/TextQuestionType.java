package OneQ.OnSurvey.domain.survey.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TextQuestionType {
    SHORT(1, 20),
    LONG(1, 500),
    NUMBER(1, 100),
    DATE(null, null); // DATE에 대해서는 min, max validation 미진행

    private final Integer min;
    private final Integer max;
}
