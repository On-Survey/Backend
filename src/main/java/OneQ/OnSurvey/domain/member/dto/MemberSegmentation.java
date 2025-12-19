package OneQ.OnSurvey.domain.member.dto;

import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.survey.model.AgeRange;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.Residence;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Set;

@Getter @ToString
@NoArgsConstructor
public class MemberSegmentation{
    private Gender gender;
    private String birthDay;
    private Residence residence;

    private Set<Interest> interests;

    public AgeRange convertBirthDayIntoAgeRange() {
        System.out.println(birthDay);

        int birthYear = Integer.parseInt(birthDay.substring(0, 4));
        int currentYear = LocalDate.now().getYear();
        int age = currentYear - birthYear + 1;

        return AgeRange.getAgeRangeByAge(age);
    }
}
