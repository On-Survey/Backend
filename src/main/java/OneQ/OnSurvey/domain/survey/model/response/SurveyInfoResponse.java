package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.survey.entity.SurveyInfo;
import OneQ.OnSurvey.domain.survey.model.AgeRange;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.Residence;

import java.util.Set;

public record SurveyInfoResponse(
        Integer dueCount,
        Integer dueCountPrice,
        Gender gender,
        Integer genderPrice,
        Set<AgeRange> ages,
        Integer agePrice,
        Residence residence,
        Integer residencePrice
) {
    public static SurveyInfoResponse from(SurveyInfo info) {
        if (info == null) return null;
        Set<AgeRange> ages = info.getAges() == null
                ? Set.of()
                : Set.copyOf(info.getAges());

        return new SurveyInfoResponse(
                info.getDueCount(),
                info.getDueCountPrice() != null ? info.getDueCountPrice() : 0,
                info.getGender(),
                info.getGenderPrice() != null ? info.getGenderPrice() : 0,
                ages,
                info.getAgePrice() != null ? info.getAgePrice() : 0,
                info.getResidence(),
                info.getResidencePrice() != null ? info.getResidencePrice() : 0
        );
    }
}
