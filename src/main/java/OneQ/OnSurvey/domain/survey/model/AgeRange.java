package OneQ.OnSurvey.domain.survey.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AgeRange {
    ALL("전체", null, null),
    TEN("10대", 10, 19),
    TWENTY("20대", 20, 29),
    THIRTY("30대", 30, 39),
    FOURTY("40대", 40, 49),
    FIFTY("50대", 50, 59),
    SIXTY("60대", 60, 69),
    OVER("70세 이상", 70, null);

    private final String value;
    private final Integer minAge;
    private final Integer maxAge;

    public static AgeRange getAgeRangeByAge(int age) {
        for (AgeRange range : AgeRange.values()) {
            if (range == ALL) {
                continue;
            }
            if (age >= range.minAge && age <= range.maxAge) {
                return range;
            } else if (range.maxAge == null) {
                return range;
            }
        }
        throw new IllegalArgumentException("해당 연령대가 존재하지 않습니다.");
    }
}