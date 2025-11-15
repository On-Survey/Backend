package OneQ.OnSurvey.domain.survey.model.response;

import OneQ.OnSurvey.domain.survey.entity.SurveyInfo;
import OneQ.OnSurvey.domain.survey.model.AgeRange;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.Residence;

public record SurveyInfoResponse(
        Gender gender,
        AgeRange age,
        Residence residence
) {
    public static SurveyInfoResponse from(SurveyInfo info) {
        if (info == null) return null;

        return new SurveyInfoResponse(
                info.getGender(),
                info.getAge(),
                info.getResidence()
        );
    }
}
