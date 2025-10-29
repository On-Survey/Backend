package OneQ.OnSurvey.domain.survey.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Gender {
    ALL("전체"),
    MALE("남성"),
    FEMALE("여성");

    private final String value;
}
