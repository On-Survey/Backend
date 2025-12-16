package OneQ.OnSurvey.domain.survey.model.dto;

import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.survey.model.AgeRange;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.Residence;

import java.util.Set;

public record SurveySegmentation(
    Gender gender,
    Set<AgeRange> ages,
    Residence residence,

    Set<Interest> interests
) {
}
