package OneQ.OnSurvey.domain.survey.controller;

import OneQ.OnSurvey.domain.survey.model.dto.GlobalStats;
import OneQ.OnSurvey.domain.survey.model.dto.OpenSurveyStats;
import OneQ.OnSurvey.domain.survey.model.response.GlobalStatsResponse;
import OneQ.OnSurvey.domain.survey.model.response.OpenSurveyStatsResponse;
import OneQ.OnSurvey.domain.survey.service.SurveyGlobalStatsService;
import OneQ.OnSurvey.domain.survey.service.query.SurveyQuery;
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
    private final SurveyQuery surveyQuery;

    @GetMapping("/global-stats")
    @Operation(summary = "전체 설문 전역 통계 조회", description = "전체 설문에 대한 총 목표 수/참여자 수/프로모션 지급자 수/일간 활성 사용자 수를 반환합니다.")
    public SuccessResponse<GlobalStatsResponse> getGlobalStats() {
        GlobalStats stats = surveyGlobalStatsService.getStats();
        return SuccessResponse.ok(GlobalStatsResponse.from(stats));
    }

    @GetMapping("/open-stats")
    @Operation(summary = "열린 설문 통계 조회", description = "현재 진행 중인 설문 수와 가장 높은 참여 보상 코인 금액을 반환합니다.")
    public SuccessResponse<OpenSurveyStatsResponse> getOpenStats() {
        OpenSurveyStats stats = surveyQuery.getOpenSurveyStats();
        return SuccessResponse.ok(OpenSurveyStatsResponse.from(stats));
    }
}
