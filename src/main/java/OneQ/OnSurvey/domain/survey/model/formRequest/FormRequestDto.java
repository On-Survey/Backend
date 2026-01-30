package OneQ.OnSurvey.domain.survey.model.formRequest;

import OneQ.OnSurvey.domain.survey.entity.FormRequest;

import java.time.LocalDate;

public record FormRequestDto(
        String formLink,
        Integer questionCount,
        Integer targetResponseCount,
        LocalDate deadline,
        String requesterEmail,
        Integer price
) {
    public FormRequest toEntity() {
        return FormRequest.createRequest(
                formLink,
                questionCount,
                targetResponseCount,
                deadline,
                requesterEmail,
                price
        );
    }
}
