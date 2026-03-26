package OneQ.OnSurvey.domain.survey.service.formRequest;

import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.survey.model.formRequest.ConversionDto;
import OneQ.OnSurvey.domain.survey.model.formRequest.event.FormRequestConversionEvent;
import OneQ.OnSurvey.domain.survey.service.command.SurveyCommand;
import OneQ.OnSurvey.global.infra.discord.notifier.AlertNotifier;
import OneQ.OnSurvey.global.infra.ncp.objectStorage.NcpS3Props;
import OneQ.OnSurvey.global.infra.transaction.TransactionHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class FormEventListener {

    private static final String CONVERSION_S3_KEY_PREFIX = "google-form-conversion/";

    private final AmazonS3 amazonS3;
    private final AlertNotifier alertNotifier;
    private final NcpS3Props ncpS3Props;
    private final ObjectMapper objectMapper;
    private final TransactionHandler transactionHandler;

    private final FormConverter formConverter;
    private final FormUpdater formUpdater;
    private final SurveyCommand surveyCommand;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void convertGoogleFormIntoSurvey(FormRequestConversionEvent event) {
        log.info("[FormEventListener] 구글폼 변환 시작 - requestId: {}, formUrl: {}", event.requestId(), event.formUrls());
        String bucket = ncpS3Props.getBucket();

        event.formUrls()
            .forEach(formUrl -> {
                String formId = formUrl.split("/")[5];
                String jsonContent;
                try {
                    S3Object s3Object = amazonS3.getObject(bucket, CONVERSION_S3_KEY_PREFIX + "form_" + formId + ".json");

                    try (S3ObjectInputStream inputStream = s3Object.getObjectContent()) {
                        jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    }
                } catch (AmazonS3Exception e) {
                    log.error("NCP Object Storage에서 파일을 찾을 수 없거나 접근할 수 없습니다. formId: {}", formId, e);
                    // 디스코드 알림
                    return;
                } catch (IOException e) {
                    log.error("NCP Object Storage 파일 스트림 읽기 실패. formId: {}", formId, e);
                    // 디스코드 알림
                    return;
                }

                ConversionDto dto;
                int convertibleCount;
                try {
                    JsonNode rootNode = objectMapper.readTree(jsonContent);
                    convertibleCount = rootNode.path("convertibleCounts").asInt();
                    dto = objectMapper.treeToValue(rootNode.path("convertibleDetails"), ConversionDto.class);
                } catch (Exception e) {
                    log.error("저장된 설문폼을 변환하지 못했습니다.", e);
                    // 디스코드 알림
                    return;
                }

                transactionHandler.runInTransaction(() -> {
                    Long conversionSurveyId = formConverter.createSurveyFromConversionResult(dto, event.memberId());

                    formUpdater.markAsRegistered(event.requestId(), conversionSurveyId, convertibleCount);

                    if (event.screening() != null) {
                        surveyCommand.upsertScreening(
                            conversionSurveyId,
                            event.screening().content(),
                            event.screening().answer()
                        );
                    }
                    if (event.interests() != null && !event.interests().isEmpty()) {
                        surveyCommand.upsertInterest(conversionSurveyId, event.interests());
                    } else {
                        surveyCommand.upsertInterest(conversionSurveyId, Set.of(Interest.BUSINESS));
                    }
                    surveyCommand.submitSurvey(event.userKey(), conversionSurveyId, event.surveyForm());

                    return conversionSurveyId;
                });
            });
    }
}
