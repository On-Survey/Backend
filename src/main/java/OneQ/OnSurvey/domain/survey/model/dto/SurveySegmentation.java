package OneQ.OnSurvey.domain.survey.model.dto;

import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.survey.model.AgeRange;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.Residence;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Set;

@Getter @ToString
@NoArgsConstructor
public class SurveySegmentation {
    private Long surveyId;

    private Gender gender;
    private Set<AgeRange> ages;
    private Residence residence;

    private Set<Interest> interests;
}
