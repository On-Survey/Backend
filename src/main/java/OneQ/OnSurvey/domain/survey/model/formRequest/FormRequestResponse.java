package OneQ.OnSurvey.domain.survey.model.formRequest;

import OneQ.OnSurvey.domain.survey.entity.FormRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record FormRequestResponse(
        Long id,
        String formLink,
        Integer questionCount,
        Integer targetResponseCount,
        LocalDate deadline,
        String requesterEmail,
        Integer price,
        Boolean isRegistered,
        Long registeredSurveyId,
        LocalDateTime createdAt
) {
    public static FormRequestResponse of(FormRequest request) {
        return new FormRequestResponse(
                request.getId(),
                request.getFormLink(),
                request.getQuestionCount(),
                request.getTargetResponseCount(),
                request.getDeadline(),
                request.getRequesterEmail(),
                request.getPrice(),
                request.getIsRegistered(),
                request.getRegisteredSurveyId(),
                request.getCreatedAt()
        );
    }
}
