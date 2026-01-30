package OneQ.OnSurvey.domain.survey.model.formRequest;

import OneQ.OnSurvey.domain.survey.entity.FormRequest;

import java.util.List;

public record FormListResponse(
        List<FormRequestResponse> requests,
        Integer totalCount
) {
    public static FormListResponse of(List<FormRequest> requests) {
        return new FormListResponse(
                requests.stream()
                        .map(FormRequestResponse::of)
                        .toList(),
                requests.size()
        );
    }
}
