package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.survey.entity.SurveyInfo;
import OneQ.OnSurvey.domain.survey.model.AgeRange;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.Residence;

public record SurveyInfoResponse(
        Integer dueCount,
        Integer dueCountPrice,
        Gender gender,
        Integer genderPrice,
        AgeRange age,
        Integer agePrice,
        Residence residence,
        Integer residencePrice
) {
    public static SurveyInfoResponse from(SurveyInfo info) {
        if (info == null) return null;

        return new SurveyInfoResponse(
                info.getDueCount(),
                info.getDueCountPrice(),
                info.getGender(),
                info.getGenderPrice(),
                info.getAge(),
                info.getAgePrice(),
                info.getResidence(),
                info.getResidencePrice()
        );
    }
}
