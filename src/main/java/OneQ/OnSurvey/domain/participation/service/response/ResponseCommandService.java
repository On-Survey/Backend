package OneQ.OnSurvey.domain.participation.service.response;

import OneQ.OnSurvey.domain.participation.entity.Response;
import OneQ.OnSurvey.domain.participation.repository.response.ResponseRepository;
import OneQ.OnSurvey.domain.survey.SurveyErrorCode;
import OneQ.OnSurvey.domain.survey.entity.SurveyInfo;
import OneQ.OnSurvey.domain.survey.repository.SurveyInfoRepository;
import OneQ.OnSurvey.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ResponseCommandService implements ResponseCommand {
    private final ResponseRepository responseRepository;
    private final SurveyInfoRepository surveyInfoRepository;

    @Override
    public Boolean createResponse(Long surveyId, Long memberId) {
        Response response = Response.of(surveyId, memberId);
        responseRepository.save(response);

        SurveyInfo surveyInfo = surveyInfoRepository.findBySurveyId(surveyId)
                .orElseThrow(() -> new CustomException(SurveyErrorCode.SURVEY_INFO_NOT_FOUND));

        surveyInfo.increaseCompletedCount();
        return true;
    }
}
