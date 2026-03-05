package OneQ.OnSurvey.domain.survey.model.response;

import java.util.List;

public record MySurveyListResponse(
        int totalCount,
        int refundedCount,
        List<MySurveyItemResponse> ongoingSurveys,
        List<MySurveyItemResponse> refundedSurveys
) { }
