package OneQ.OnSurvey.domain.survey.model.request;

import java.util.List;

public record SurveyParticipationCompletionRequest(
    List<Integer> visitedSections
) {
}
