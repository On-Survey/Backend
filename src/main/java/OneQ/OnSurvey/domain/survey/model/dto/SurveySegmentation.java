package OneQ.OnSurvey.domain.survey.model.dto;

import OneQ.OnSurvey.domain.survey.model.AgeRange;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.Residence;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Set;

@Getter @ToString
@NoArgsConstructor
@AllArgsConstructor
public class SurveySegmentation {
    private Long surveyId;

    private Gender gender;
    private Set<AgeRange> ages;
    private Residence residence;

    // Set<Interest> interests
}
