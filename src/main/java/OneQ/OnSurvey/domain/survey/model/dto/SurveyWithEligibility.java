package OneQ.OnSurvey.domain.survey.model.dto;

import OneQ.OnSurvey.domain.member.value.Interest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;

@Getter @ToString
@NoArgsConstructor
public class SurveyWithEligibility {
    private Long surveyId;
    private Long memberId;
    private String title;
    private String description;
    private Boolean isFree;
    private Set<Interest> interests;
    private LocalDateTime deadline;
    private Boolean isEligible;
}

