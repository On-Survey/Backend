package OneQ.OnSurvey.domain.survey.model.formRequest;

import OneQ.OnSurvey.domain.survey.entity.FormRequest;

import java.time.LocalDateTime;

public record FormRequestResponse(
        Long id,
        String formLink,
        String requesterEmail,
        Integer questionCount,
        Integer targetResponseCount,
        Integer price,
        Boolean isRegistered,
        Long registeredSurveyId,
        LocalDateTime createdAt
) {
    public static FormRequestResponse of(FormRequest request) {
        return new FormRequestResponse(
                request.getId(),
                request.getFormLink(),
                request.getRequesterEmail(),
                request.getQuestionCount(),
                request.getTargetResponseCount(),
                request.getPrice(),
                request.getIsRegistered(),
                request.getRegisteredSurveyId(),
                request.getCreatedAt()
        );
    }
}
