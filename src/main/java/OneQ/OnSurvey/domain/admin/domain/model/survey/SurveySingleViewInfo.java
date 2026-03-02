package OneQ.OnSurvey.domain.admin.domain.model.survey;

import java.time.LocalDate;
import java.util.Set;

public record SurveySingleViewInfo(
    Long surveyId,
    String title,
    String description,
    LocalDate deadline,
    String imageUrl,

    Set<String> ages,
    String gender,
    String residence,
    Set<String> interests,
    Integer dueCount
) {
}
