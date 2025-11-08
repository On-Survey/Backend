package OneQ.OnSurvey.domain.survey.controller;

import OneQ.OnSurvey.domain.question.service.QuestionQuery;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.domain.survey.model.response.ParticipationQuestionResponse;
import OneQ.OnSurvey.domain.survey.model.response.SurveyParticipationResponse;
import OneQ.OnSurvey.domain.survey.service.SurveyQuery;
import OneQ.OnSurvey.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/survey-participation")
@RequiredArgsConstructor
public class ParticipationController {

    private final SurveyQuery surveyQueryService;
    private final QuestionQuery questionQueryService;

    @GetMapping("surveys/ongoing")
    @Operation(summary = "노출 중인 설문을 조회합니다.")
    public SuccessResponse<SurveyParticipationResponse> getSurveyListOnGoing(
        @RequestParam(required = false) Long lastSurveyId,
        @RequestParam(defaultValue = "15") Integer size
    ) {
        Pageable pageable = PageRequest.of(0, size, Sort.by("surveyId"));
        return SuccessResponse.ok(
            surveyQueryService.getParticipationSurveyList(
                SurveyStatus.ONGOING, lastSurveyId, pageable
            )
        );
    }

    @GetMapping("surveys")
    @Operation(summary = "선택한 설문을 조회합니다.")
    public SuccessResponse<ParticipationQuestionResponse> getQuestionsOfSurveyId(
        @RequestParam Long surveyId
    ) {
        return SuccessResponse.ok(questionQueryService.getQuestionListBySurveyId(surveyId));
    }
}
