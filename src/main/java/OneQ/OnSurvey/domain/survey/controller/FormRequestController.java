package OneQ.OnSurvey.domain.survey.controller;

import OneQ.OnSurvey.domain.survey.model.formRequest.FormListResponse;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormRequestDto;
import OneQ.OnSurvey.domain.survey.service.formRequest.FormCreator;
import OneQ.OnSurvey.domain.survey.service.formRequest.FormFinder;
import OneQ.OnSurvey.domain.survey.service.formRequest.FormUpdater;
import OneQ.OnSurvey.global.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/form-requests")
public class FormRequestController {

    private final FormCreator formCreator;
    private final FormFinder formFinder;
    private final FormUpdater formUpdater;

    @PostMapping
    @Operation(summary = "폼 등록 신청", description = "폼을 등록하기 위한 신청을 생성합니다.")
    public SuccessResponse<Long> createGoogleFormRequest(
            @RequestBody FormRequestDto request
    ) {
        Long requestId = formCreator.createFormRequest(request);
        return SuccessResponse.ok(requestId);
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
        formUpdater.markAsRegistered(requestId, surveyId);
        return SuccessResponse.ok("폼이 온서베이에 등록되었습니다.");
    }
}
