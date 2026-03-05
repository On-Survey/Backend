package OneQ.OnSurvey.domain.admin.domain.model.survey;

import java.time.LocalDateTime;

/* 설문 목록 조회 시 사용하는 VO */
public record AdminSurveyListView(
    Long surveyId,
    String title,
    AdminSurveyStatus status,
    Long memberId, // 설문 소유자
    LocalDateTime createdAt
) { }
