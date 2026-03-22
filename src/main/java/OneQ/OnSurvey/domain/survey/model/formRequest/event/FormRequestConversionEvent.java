package OneQ.OnSurvey.domain.survey.model.formRequest.event;

import OneQ.OnSurvey.domain.survey.model.request.ScreeningRequest;
import OneQ.OnSurvey.domain.survey.model.request.SurveyFormRequest;

import java.util.List;

public record FormRequestConversionEvent (
    Long requestId,
    Long userKey,
    Long memberId,
    List<String> formUrls,

    // TODO 무거운 이벤트를 requestId, surveyId, userKey, formUrls만 보내도록 설문을 선생성 후 데이터를 추가하도록 로직 수정 필요
    ScreeningRequest screening,
    SurveyFormRequest surveyForm
) {
}
