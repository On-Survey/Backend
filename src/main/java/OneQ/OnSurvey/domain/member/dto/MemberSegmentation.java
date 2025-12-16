package OneQ.OnSurvey.domain.member.dto;

import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.survey.model.AgeRange;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.Residence;

import java.time.LocalDate;
import java.util.Set;

public record MemberSegmentation(
    Long userKey,

    Gender gender,
    String birthday,
    Residence residence,

    Set<Interest> interests
) {

    public AgeRange convertBirthdayIntoAgeRange() {
        int birthYear = Integer.parseInt(birthday.substring(0, 4));
        int currentYear = LocalDate.now().getYear();
        int age = currentYear - birthYear + 1;

        return AgeRange.getAgeRangeByAge(age);
    }
}
