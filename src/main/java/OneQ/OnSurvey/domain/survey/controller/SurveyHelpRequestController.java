package OneQ.OnSurvey.domain.survey.controller;

import OneQ.OnSurvey.global.auth.custom.CustomUserDetails;
import OneQ.OnSurvey.global.common.response.SuccessResponse;
import OneQ.OnSurvey.global.infra.discord.notifier.AlertNotifier;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.SurveyHelpRequestAlert;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/surveys/help-requests")
@RequiredArgsConstructor
public class SurveyHelpRequestController {

    private final AlertNotifier alertNotifier;

    @PostMapping
    @Operation(summary = "설문 반려 도움 요청", description = "설문이 반려된 경우 운영팀에 도움을 요청합니다.")
    public SuccessResponse<Void> requestHelp(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody @Valid HelpRequest request
    ) {
        alertNotifier.sendSurveyHelpRequestAsync(new SurveyHelpRequestAlert(
                request.email(),
                request.name(),
                request.rejectionReasons(),
                request.content()
        ));
        return SuccessResponse.ok(null);
    }

    public record HelpRequest(
            @NotBlank String email,
            @NotBlank String name,
            @NotEmpty List<String> rejectionReasons,
            @NotBlank String content
    ) {}
}
