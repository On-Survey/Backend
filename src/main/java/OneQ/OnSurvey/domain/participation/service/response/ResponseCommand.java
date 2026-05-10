package OneQ.OnSurvey.domain.participation.service.response;

import OneQ.OnSurvey.domain.participation.model.dto.ParticipationCompletionDto;

public interface ResponseCommand {
    Boolean createResponse(ParticipationCompletionDto dto);
}
