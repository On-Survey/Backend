package OneQ.OnSurvey.domain.survey.model.request;

import OneQ.OnSurvey.domain.survey.model.AgeRange;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.Residence;

public record SurveyFormRequest (
        String title,
        String description,
        Integer totalCoin,
        Gender gender,
        AgeRange age,
        Residence residence,
        Integer dueCount
        // TODO 가격 추가
) {
}
