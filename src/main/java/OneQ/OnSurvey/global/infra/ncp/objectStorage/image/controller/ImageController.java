package OneQ.OnSurvey.global.infra.ncp.objectStorage.image.controller;

import OneQ.OnSurvey.global.auth.custom.CustomUserDetails;
import OneQ.OnSurvey.global.common.response.SuccessResponse;
import OneQ.OnSurvey.global.infra.ncp.objectStorage.image.dto.ImageUploadResponse;
import OneQ.OnSurvey.global.infra.ncp.objectStorage.image.service.ImageModifyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/images")
public class ImageController {

    private final ImageModifyService imageModifyService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이미지 업로드", description = "이미지 업로드 후 Public URL을 반환합니다. URL은 이후 게시글 엔티티의 POST 요청에서 image_url 필드로 저장합니다.")
    public SuccessResponse<ImageUploadResponse> upload(
            @Parameter(description = "업로드할 이미지 파일", required = true,
                    schema = @Schema(type = "string", format = "binary"))
            @RequestPart("file") MultipartFile file,

            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        ImageUploadResponse res = imageModifyService.upload(file, principal.getUserKey());
        return SuccessResponse.ok(res);
    }
}