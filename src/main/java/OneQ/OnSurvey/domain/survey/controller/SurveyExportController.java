package OneQ.OnSurvey.domain.survey.controller;

import OneQ.OnSurvey.domain.survey.model.export.SurveyExportFile;
import OneQ.OnSurvey.domain.survey.service.export.SurveyExport;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ByteArrayResource> export(@PathVariable Long surveyId) {
        SurveyExportFile file = surveyExport.exportCsv(surveyId);

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
