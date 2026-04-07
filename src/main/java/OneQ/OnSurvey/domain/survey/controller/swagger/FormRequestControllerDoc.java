package OneQ.OnSurvey.domain.survey.controller.swagger;

import OneQ.OnSurvey.domain.survey.model.formRequest.FormValidationRequestDto;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormValidationResponse;
import OneQ.OnSurvey.global.auth.custom.Authenticatable;
import OneQ.OnSurvey.global.common.response.ErrorResponse;
import OneQ.OnSurvey.global.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface FormRequestControllerDoc {

    @PostMapping("/validation")
    @Operation(summary = "폼 링크 유효성 검사 및 미리보기 반환", description = "구글 폼 편집 URL 유효성 검사를 진행하여 변환 가능한 문항 수, 변환 불가능 사유, 미리보기 데이터 등을 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(responseCode = "409", description = "유효성 검사 진행 중",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "폼 변환 중복요청", value = "{ \"code\": \"FORM_REQUEST_003\", \"message\": \"구글 폼 링크 유효성 검사를 진행 중입니다.\" }")
                }
            )
        ),
        @ApiResponse(responseCode = "429", description = "이메일 시간 당 한도 초과",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "폼 변환 이메일 시간 당 한도 초과", value = "{ \"code\": \"FORM_REQUEST_004\", \"message\": \"구글 폼 링크 유효성 검사 이메일 시간 당 한도를 초과했습니다. 잠시 후 시도해주세요.\" }")
                }
            )
        ),
        @ApiResponse(responseCode = "502", description = "유효성 검사 실패",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "폼 링크 유효성 검사 실패", value = "{ \"code\": \"FORM_REQUEST_005\", \"message\": \"구글 폼 링크 유효성 검사가 정상적으로 수행되지 않았습니다.\" }")
                }
            )
        ),
    })
    SuccessResponse<FormValidationResponse> getConvertableCounts(
        @RequestBody @Valid FormValidationRequestDto request,
        @AuthenticationPrincipal Authenticatable principal
    );
}
