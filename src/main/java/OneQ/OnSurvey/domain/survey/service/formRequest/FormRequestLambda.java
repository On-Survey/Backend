package OneQ.OnSurvey.domain.survey.service.formRequest;

import OneQ.OnSurvey.domain.survey.SurveyErrorCode;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormConversionPayload;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormConversionResponse;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormValidationAndStashResponse;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormValidationPayload;
import OneQ.OnSurvey.global.common.exception.CustomException;
import OneQ.OnSurvey.global.infra.discord.notifier.AlertNotifier;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.SurveyConversionAlert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class FormRequestLambda {

    private static final long FORM_CONVERSION_REQUEST_TIMEOUT = 20L;

    @Value("${external.lambda.survey-conversion.url:}")
    private String conversionUrl;
    @Value("${external.lambda.google-form-validation.url:}")
    private String validationUrl;

    private final AlertNotifier alertNotifier;
    @Qualifier("lambdaWebClient")
    private final WebClient webClient;

    public FormConversionResponse convertGoogleFormIntoSurvey(FormConversionPayload payload) {
        FormConversionResponse response = webClient.post()
            .uri(conversionUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(FormConversionResponse.class)
            .timeout(Duration.ofSeconds(FORM_CONVERSION_REQUEST_TIMEOUT))
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(3)))
            .onErrorMap(e -> {
                log.error("[FormRequestLambda] 구글폼 변환 중 오류 발생 - requestId: {}, error: {}", payload.requestId(), e.getMessage(), e);
                alertNotifier.sendSurveyConversionAsync(
                    SurveyConversionAlert.error(1, 0, "구글폼 변환 중 오류가 발생했습니다. error: " + e.getMessage())
                );
                throw new CustomException(SurveyErrorCode.FORM_CONVERSION_FAILED);
            })
            .block();

        return response;
    }

    public FormValidationAndStashResponse validateAndStashFormRequest(FormValidationPayload payload) {

        FormValidationAndStashResponse response = webClient.post()
            .uri(validationUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(FormValidationAndStashResponse.class)
            .timeout(Duration.ofSeconds(FORM_CONVERSION_REQUEST_TIMEOUT))
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(3)))
            .onErrorMap(e -> {
                log.error("[FormRequestLambda:validateAndStashFormRequest] 구글폼 링크 유효성 검사 실패 - URLs: {}, error: {}", payload.urls(), e.getMessage(), e);
                throw new CustomException(SurveyErrorCode.FORM_VALIDATION_FAILED);
            })
            .block();

        return response;
    }
}
