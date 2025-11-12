package OneQ.OnSurvey.domain.participation.service.response;

import OneQ.OnSurvey.domain.participation.entity.Response;

public interface ResponseCommand {
    Boolean createResponse(Long surveyId, Long memberId);
    Response settleResponse(Long surveyId, Long memberId);
}
