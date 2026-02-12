package OneQ.OnSurvey.domain.survey.controller;

import OneQ.OnSurvey.domain.survey.controller.swagger.FormControllerDoc;
import OneQ.OnSurvey.domain.survey.model.request.*;
import OneQ.OnSurvey.domain.survey.model.response.*;
import OneQ.OnSurvey.domain.survey.service.form.SurveyFormFacade;
import OneQ.OnSurvey.global.auth.custom.Authenticatable;
import OneQ.OnSurvey.global.auth.custom.CustomUserDetails;
import OneQ.OnSurvey.global.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/survey-form")
@RequiredArgsConstructor
@Slf4j
public class FormController implements FormControllerDoc {

    private final SurveyFormFacade surveyFormFacade;

    @PostMapping("/surveys")
    @Operation(summary = "설문 폼을 생성합니다.")
    public SuccessResponse<SurveyFormResponse> createSurvey(
        @AuthenticationPrincipal Authenticatable principal,
        @RequestBody SurveyFormCreateRequest request
    ) {
        return SuccessResponse.ok(surveyFormFacade.createSurvey(principal.getMemberId(), request));
    }

    @PatchMapping("/surveys/{surveyId}/display")
    @Operation(summary = "설문 제목과 상세설명을 수정합니다.")
    public SuccessResponse<SurveyFormResponse> updateSurvey(
        @AuthenticationPrincipal Authenticatable principal,
        @RequestBody SurveyFormCreateRequest request,
        @PathVariable Long surveyId
    ) {
        return SuccessResponse.ok(surveyFormFacade.updateSurveyDisplay(principal.getMemberId(), surveyId, request));
    }

    @PostMapping("/surveys/{surveyId}/questions")
    @Operation(summary = "새로운 문항을 생성합니다.")
    public SuccessResponse<CreateQuestionResponse> createQuestion(
        @RequestBody QuestionRequest request,
        @PathVariable Long surveyId
    ) {
        return SuccessResponse.ok(surveyFormFacade.createQuestion(surveyId, request));
    }

    @PutMapping("/surveys/{surveyId}/questions")
    @Operation(summary = "기존 문항을 수정합니다. (임시저장)")
    public SuccessResponse<UpdateQuestionResponse> updateSurvey(
        @RequestBody QuestionRequest request,
        @PathVariable Long surveyId
    ) {
        return SuccessResponse.ok(surveyFormFacade.upsertQuestions(surveyId, request));
    }

    @PatchMapping("/surveys/{surveyId}")
    @Operation(summary = "폼을 완성합니다.")
    public SuccessResponse<SurveyFormResponse> completeSurvey(
        @AuthenticationPrincipal Authenticatable details,
        @PathVariable Long surveyId,
        @RequestBody @Valid SurveyFormRequest request
    ) {
        return SuccessResponse.ok(surveyFormFacade.completeSurvey(details.getUserKey(), surveyId, request));
    }

    @PatchMapping("/surveys/{surveyId}/free")
    @Operation(summary = "무료 설문 폼을 완성합니다.")
    public SuccessResponse<SurveyFormResponse> completeFreeSurvey(
            @AuthenticationPrincipal CustomUserDetails details,
            @PathVariable Long surveyId,
            @RequestBody @Valid FreeSurveyFormRequest request
    ) {
        return SuccessResponse.ok(surveyFormFacade.completeFreeSurvey(details.getUserKey(), surveyId, request));
    }

    @PatchMapping("/surveys/{surveyId}/interests")
    @Operation(summary = "설문의 관심사를 등록합니다.")
    public SuccessResponse<InterestResponse> updateInterest(
        @RequestBody SurveyInterestRequest request,
        @PathVariable Long surveyId
    ) {
        return SuccessResponse.ok(surveyFormFacade.updateInterest(surveyId, request));
    }

    @PostMapping("/surveys/{surveyId}/screenings")
    @Operation(summary = "설문에 대한 스크리닝 문항을 생성합니다.")
    public SuccessResponse<ScreeningResponse> createScreening(
        @RequestBody ScreeningRequest request,
        @PathVariable Long surveyId
    ) {
        return SuccessResponse.ok(surveyFormFacade.createScreening(surveyId, request));
    }

    @PutMapping("/surveys/{surveyId}/sections")
    @Operation(summary = "설문에 대한 섹션을 생성/수정하고, 삭제되는 섹션 내의 문항들을 삭제합니다.")
    public SuccessResponse<SectionResponse> createSection(
        @RequestBody SectionRequest request,
        @PathVariable Long surveyId
    ) {
        return SuccessResponse.ok(surveyFormFacade.upsertSection(surveyId, request));
    }
}
