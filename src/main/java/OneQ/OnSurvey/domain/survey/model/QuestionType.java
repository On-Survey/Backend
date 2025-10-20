package OneQ.OnSurvey.domain.survey.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum QuestionType {
    CHOICE("객관식"),
    TEXT("주관식"),
    EVALUATION("평가식"),
    NPS("NPS");

    private final String value;
}
