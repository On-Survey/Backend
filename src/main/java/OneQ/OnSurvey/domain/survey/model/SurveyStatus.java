package OneQ.OnSurvey.domain.survey.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SurveyStatus {
    ONGOING("노출중"),
    WRITING("작성중"),
    CLOSED("마감"),
    REVIEW("검수중");

    private final String value;
}
