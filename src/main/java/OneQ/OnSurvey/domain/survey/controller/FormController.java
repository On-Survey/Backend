package OneQ.OnSurvey.domain.survey.controller;

import OneQ.OnSurvey.domain.member.service.MemberFinder;
import OneQ.OnSurvey.domain.question.model.QuestionType;
import OneQ.OnSurvey.domain.question.service.QuestionCommand;
import OneQ.OnSurvey.domain.survey.model.request.QuestionRequest;
import OneQ.OnSurvey.domain.survey.model.request.ScreeningRequest;
import OneQ.OnSurvey.domain.survey.model.request.SurveyFormRequest;
import OneQ.OnSurvey.domain.survey.model.response.CreateQuestionResponse;
import OneQ.OnSurvey.domain.survey.model.response.ScreeningResponse;
import OneQ.OnSurvey.domain.survey.model.response.SurveyFormResponse;
import OneQ.OnSurvey.domain.survey.model.response.UpdateQuestionResponse;
import OneQ.OnSurvey.domain.survey.service.SurveyCommand;
import OneQ.OnSurvey.global.auth.custom.CustomUserDetails;
import OneQ.OnSurvey.global.exception.CustomException;
import OneQ.OnSurvey.global.exception.ErrorCode;
import OneQ.OnSurvey.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/survey-form")
@RequiredArgsConstructor
public class FormController {
    private final SurveyCommand surveyCommand;
    private final QuestionCommand questionCommand;

    private final MemberFinder memberFinder;

    @PostMapping("surveys")
    @Operation(summary = "설문 폼을 생성합니다.")
    public SuccessResponse<SurveyFormResponse> createSurvey(
        @AuthenticationPrincipal CustomUserDetails details,
        @RequestBody SurveyFormRequest request
    ) {
        Long memberId = memberFinder.getMemberByUserKey(details.getUserKey()).getId();

        SurveyFormResponse response = surveyCommand.upsertSurvey(
            null, request.title(), request.description(), memberId);

        return SuccessResponse.ok(response);
    }

    @PostMapping("surveys/{surveyId}/questions")
    @Operation(summary = "새로운 문항을 생성합니다.")
    public SuccessResponse<CreateQuestionResponse> upsertSurvey(
        @RequestBody QuestionRequest request,
        @PathVariable Long surveyId
    ) {
        String type = request.info().getFirst().getQuestionType().getValue();
        if (type == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        CreateQuestionResponse mockResponse = new CreateQuestionResponse(
            surveyId,
            101L,
            request.info().getFirst().getQuestions().getFirst().getQuestionOrder(),
            request.info().getFirst().getQuestions().getFirst().getTitle(),
            QuestionType.fromKey(type)
        );

        return SuccessResponse.ok(mockResponse);
    }

    @PutMapping("surveys/{surveyId}/questions")
    @Operation(summary = "기존 문항을 수정합니다. (임시저장)")
    public SuccessResponse<UpdateQuestionResponse> updateSurvey(
        @RequestBody QuestionRequest request,
        @PathVariable Long surveyId
    ) {
        UpdateQuestionResponse mockResponse = new UpdateQuestionResponse(
            surveyId,
            request.info()
        );

        return SuccessResponse.ok(mockResponse);
    }

    @PatchMapping("surveys/{surveyId}")
    @Operation(summary = "폼을 완성합니다.")
    public SuccessResponse<Boolean> completeSurvey(
        @PathVariable Long surveyId
    ) {
        return SuccessResponse.ok(surveyCommand.submitSurvey(surveyId));
    }

    @PostMapping("surveys/{surveyId}/screenings")
    @Operation(summary = "설문에 대한 스크리닝 문항을 생성합니다.")
    public SuccessResponse<ScreeningResponse> createScreening(
        @RequestBody ScreeningRequest request,
        @PathVariable Long surveyId
    ) {
        ScreeningResponse mockResponse = new ScreeningResponse(
            1001L,
            surveyId,
            request.content(),
            request.answer()
        );

        return SuccessResponse.ok(mockResponse);
    }
}
