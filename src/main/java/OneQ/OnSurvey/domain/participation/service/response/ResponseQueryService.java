package OneQ.OnSurvey.domain.participation.service.response;

import OneQ.OnSurvey.domain.participation.entity.Response;
import OneQ.OnSurvey.domain.participation.repository.response.ResponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResponseQueryService implements ResponseQuery {
    private final ResponseRepository responseRepository;

    @Override
    public List<Response> getResponsesByMemberId(Long memberId) {
        return responseRepository.getResponsesByMemberId(memberId);
    }

    @Override
    public List<Response> getResponsesBySurveyId(Long surveyId) {
        return responseRepository.getResponsesBySurveyId(surveyId);
    }

    @Override
    public Response getResponseBySurveyIdAndMemberId(Long surveyId, Long memberId) {
        return responseRepository.getResponseBySurveyIdAndMemberId(surveyId, memberId);
    }

    @Override
    public Integer getResponseCountBySurveyId(Long surveyId) {
        log.info("[RESPONSE:QUERY:getResponseCountBySurveyId] 설문 응답 수 - surveyId: {}", surveyId);

        return responseRepository.getResponseCountBySurveyId(surveyId);
    }

    @Override
    public Map<Long, Long> getResponseCountsBySurveyIds(Collection<Long> surveyIds) {
        log.info("[RESPONSE:QUERY:getResponseCountsBySurveyIds] 설문 응답 수 일괄 조회 - surveyIds: {}", surveyIds);

        return responseRepository.getResponseCountsBySurveyIds(surveyIds);
    }
}
