package OneQ.OnSurvey.domain.survey.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AgeRange {
    ALL("전체"),
    TEN("10대"),
    TWENTY("20대"),
    THIRTY("30대"),
    FOURTY("40대"),
    FIFTY("50대"),
    SIXTY("60대"),
    OVER("70세 이상");

    private final String value;
}