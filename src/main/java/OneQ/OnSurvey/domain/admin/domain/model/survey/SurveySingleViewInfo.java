package OneQ.OnSurvey.domain.admin.domain.model.survey;

import java.time.LocalDate;
import java.util.Set;

public record SurveySingleViewInfo(
    Long surveyId,
    String title,
    String description,
    LocalDate deadline,

    Set<String> ages,
    String gender,
    Set<String> residences,
    Set<String> interests,
    Integer dueCount
) {
}
