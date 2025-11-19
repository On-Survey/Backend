package OneQ.OnSurvey.domain.participation.service.response;

public interface ResponseCommand {
    Boolean createResponse(Long surveyId, Long memberId);
}
