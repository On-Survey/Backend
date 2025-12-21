package OneQ.OnSurvey.domain.survey.controller;

import OneQ.OnSurvey.domain.survey.model.export.SurveyExportFile;
import OneQ.OnSurvey.domain.survey.service.export.SurveyExport;
import OneQ.OnSurvey.global.auth.custom.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/v1/surveys")
@RequiredArgsConstructor
public class SurveyExportController {

    private final SurveyExport surveyExport;

    @GetMapping("/{surveyId}/export")
    @Operation(summary = "특정 설문의 응답 데이터를 CSV 파일로 내보냅니다.")
    public ResponseEntity<ByteArrayResource> export(
            @PathVariable Long surveyId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        SurveyExportFile file = surveyExport.exportCsv(surveyId, principal.getMemberId());

        ByteArrayResource resource = new ByteArrayResource(file.bytes());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(file.filename(), StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .contentLength(file.bytes().length)
                .body(resource);
    }
}
