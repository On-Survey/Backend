package OneQ.OnSurvey.domain.survey.model.dto;

import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record SurveySearchQuery (
    List<SurveyStatus> status,
    String title,
    Long creator,
    LocalDate startDate,
    LocalDate endDate
) {
}
