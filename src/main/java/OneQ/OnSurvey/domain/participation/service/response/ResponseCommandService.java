package OneQ.OnSurvey.domain.participation.service.response;

import OneQ.OnSurvey.domain.participation.entity.Response;
import OneQ.OnSurvey.domain.participation.repository.response.ResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ResponseCommandService implements ResponseCommand {
    private final ResponseRepository responseRepository;

    @Override
    public Response createResponse(Long surveyId, Long memberId) {
        Response response = Response.of(surveyId, memberId);

        return responseRepository.save(response);
    }

    @Override
    public Response settleResponse(Long surveyId, Long memberId) {
        Response response = responseRepository.getResponseBySurveyIdAndMemberId(surveyId, memberId);
        response.settlementCompleted();

        return responseRepository.save(response);
    }
}
