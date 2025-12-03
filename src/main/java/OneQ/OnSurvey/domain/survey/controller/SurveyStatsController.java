package OneQ.OnSurvey.domain.survey.controller;

import OneQ.OnSurvey.domain.survey.entity.SurveyGlobalStats;
import OneQ.OnSurvey.domain.survey.model.response.SurveyGlobalStatsResponse;
import OneQ.OnSurvey.domain.survey.service.SurveyGlobalStatsService;
import OneQ.OnSurvey.global.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/surveys")
@RequiredArgsConstructor
public class SurveyStatsController {

    private final SurveyGlobalStatsService surveyGlobalStatsService;

    @GetMapping("/global-stats")
    @Operation(summary = "전체 설문 전역 통계 조회", description = "전체 설문에 대한 총 목표 수/참여자 수/프로모션 지급자 수를 반환합니다.")
    public SuccessResponse<SurveyGlobalStatsResponse> getGlobalStats() {
        SurveyGlobalStats stats = surveyGlobalStatsService.getStats();
        return SuccessResponse.ok(SurveyGlobalStatsResponse.from(stats));
    }
}
