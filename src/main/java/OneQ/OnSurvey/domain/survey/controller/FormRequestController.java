package OneQ.OnSurvey.domain.survey.controller;

import OneQ.OnSurvey.domain.survey.model.formRequest.FormValidationResponse;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormListResponse;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormValidationRequestDto;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormPublishRequest;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormRequestDto;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormRequestResponse;
import OneQ.OnSurvey.domain.survey.model.response.SurveyFormResponse;
import OneQ.OnSurvey.domain.survey.service.formRequest.FormCreator;
import OneQ.OnSurvey.domain.survey.service.formRequest.FormFinder;
import OneQ.OnSurvey.domain.survey.service.formRequest.FormPublisher;
import OneQ.OnSurvey.domain.survey.service.formRequest.FormUpdater;
import OneQ.OnSurvey.global.common.response.PageResponse;
import OneQ.OnSurvey.global.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/form-requests")
public class FormRequestController {

    private final FormCreator formCreator;
    private final FormFinder formFinder;
    private final FormUpdater formUpdater;
    private final FormPublisher formPublisher;

    @PostMapping
    @Operation(summary = "폼 등록 신청", description = "폼을 등록하기 위한 신청을 생성합니다.")
    public SuccessResponse<Long> createGoogleFormRequest(
            @RequestBody FormRequestDto request
    ) {
        return SuccessResponse.ok(formCreator.createFormRequest(request));
    }

    @GetMapping
    @Operation(summary = "폼 요청 목록 조회", description = "전체 폼 요청 목록을 페이지네이션하여 조회합니다. 이메일 검색 및 등록 상태 필터링이 가능합니다.")
    public PageResponse<FormRequestResponse> getFormRequests(
            @Parameter(description = "신청자 이메일 검색 (부분 일치)")
            @RequestParam(required = false) String email,
            @Parameter(description = "등록 상태 필터 (null: 전체, true: 등록완료, false: 미등록)")
            @RequestParam(required = false) Boolean isRegistered,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("[FormRequest] 목록 조회 - email: {}, isRegistered: {}, page: {}, size: {}",
                email, isRegistered, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return PageResponse.ok(formFinder.getFormRequests(email, isRegistered, pageable));
    }

    @GetMapping("/unregistered")
    @Operation(summary = "미등록 구글 폼 목록 조회", description = "아직 등록되지 않은 폼 신청 목록을 조회합니다.")
    public SuccessResponse<FormListResponse> getUnregisteredRequests() {
        return SuccessResponse.ok(formFinder.getAllUnregisteredRequests());
    }

    @PostMapping("/{requestId}/register")
    @Operation(summary = "폼 등록 완료 처리", description = "요청한 폼을 등록합니다.")
    public SuccessResponse<String> markAsRegistered(
            @PathVariable Long requestId,
            @RequestParam Long surveyId
    ) {
        formUpdater.markAsRegistered(requestId, surveyId, null);
        return SuccessResponse.ok("폼이 온서베이에 등록되었습니다.");
    }

    @PostMapping("/validation")
    @Operation(summary = "폼 링크 유효성 검사", description = "구글 폼 편집 URL로부터 전체 문항 수 중 변환 가능한 문항 수를 리턴합니다. 변환 불가능한 문항 존재 시 관련 정보를 추가로 반환합니다.")
    public SuccessResponse<FormValidationResponse> getConvertableCounts(
        @RequestBody FormValidationRequestDto request
    ) {
        log.info("[FormRequest] 폼 링크 유효성 검사 - URL: {}, requester: {}", request.formLink(), request.requesterEmail());

        FormValidationResponse response = formCreator.validationFormRequestLink(request);
        return SuccessResponse.ok(response);
    }

    @PatchMapping("/{requestId}/publish")
    @Operation(summary = "폼 설문 발행", description = "변환 완료된 설문에 스크리닝 및 세그먼트 정보를 적용하고 발행합니다.")
    public SuccessResponse<SurveyFormResponse> publishFormRequest(
            @PathVariable Long requestId,
            @RequestBody @Valid FormPublishRequest request
    ) {
        return SuccessResponse.ok(formPublisher.publishFormRequest(requestId, request));
    }
}
