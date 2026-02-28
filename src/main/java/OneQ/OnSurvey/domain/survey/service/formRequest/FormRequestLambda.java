package OneQ.OnSurvey.domain.survey.service.formRequest;

import OneQ.OnSurvey.domain.question.model.QuestionType;
import OneQ.OnSurvey.domain.question.model.dto.OptionDto;
import OneQ.OnSurvey.domain.question.model.dto.OptionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.QuestionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.SectionDto;
import OneQ.OnSurvey.domain.question.service.QuestionCommand;
import OneQ.OnSurvey.domain.survey.SurveyErrorCode;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormConversionPayload;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormConversionResponse;
import OneQ.OnSurvey.domain.survey.model.formRequest.event.FormRequestConversionEvent;
import OneQ.OnSurvey.domain.survey.model.request.SurveyFormCreateRequest;
import OneQ.OnSurvey.domain.survey.model.response.SurveyFormResponse;
import OneQ.OnSurvey.domain.survey.service.command.SurveyCommand;
import OneQ.OnSurvey.global.common.exception.CustomException;
import OneQ.OnSurvey.global.infra.discord.notifier.AlertNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class FormRequestLambda {

    private static final Long SYSTEM_MEMBER_ID = 1L; // 시스템 사용자 ID

    @Value("${external.lambda.survey-conversion.url}")
    private String lambdaUrl;

    private final AlertNotifier alertNotifier;
    private final WebClient webClient;

    private final FormUpdater formUpdater;
    private final SurveyCommand surveyCommand;
    private final QuestionCommand questionCommand;

    @Async
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void convertGoogleFormIntoSurvey(FormRequestConversionEvent event) {
        log.info("[FormRequestLambda] 구글폼 변환 시작 - requestId: {}, formUrl: {}", event.requestId(), event.formUrls());

        FormConversionPayload payload = new FormConversionPayload(event.requestId(), event.formUrls());

        FormConversionResponse response = webClient.post()
            .uri(lambdaUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(FormConversionResponse.class)
            .block();

        if (response == null || response.results() == null) {
            log.error("[FormRequestLambda] 구글폼 변환 실패 - 응답이 null입니다. requestId: {}", event.requestId());
            throw new CustomException(SurveyErrorCode.FORM_CONVERSION_FAILED);
        }

        response.results().forEach(result -> {
            if (result.isSuccess()) {
                try {
                    Long surveyId = createSurveyFromConversionResult(result);
                    formUpdater.markAsRegistered(event.requestId(), surveyId);
                    log.info("[FormRequestLambda] 구글폼 변환 성공 - requestId: {}, surveyId: {}", event.requestId(), surveyId);

                    if (result.unsupportedQuestions() != null && !result.unsupportedQuestions().isEmpty()) {
                        log.warn("[FormRequestLambda] 지원하지 않는 문항 존재 - requestId: {}, count: {}",
                            event.requestId(), result.unsupportedQuestions().size());
                    }
                } catch (Exception e) {
                    log.error("[FormRequestLambda] 설문 생성 중 오류 발생 - requestId: {}, error: {}",
                        event.requestId(), e.getMessage(), e);
                }
            } else {
                log.error("[FormRequestLambda] 구글폼 변환 실패 - requestId: {}, url: {}, message: {}",
                    event.requestId(), result.url(), result.message());
            }
        });
    }

    private Long createSurveyFromConversionResult(FormConversionResponse.Result result) {
        FormConversionResponse.Survey survey = result.survey();

        // 1. 설문 생성
        SurveyFormCreateRequest surveyRequest = new SurveyFormCreateRequest(
            survey.title(),
            survey.description()
        );
        SurveyFormResponse surveyResponse = surveyCommand.upsertSurvey(SYSTEM_MEMBER_ID, null, surveyRequest);
        Long surveyId = surveyResponse.surveyId();

        log.info("[FormRequestLambda] 설문 생성 완료 - surveyId: {}, title: {}", surveyId, survey.title());

        // 2. 섹션 생성
        if (survey.sections() != null && !survey.sections().isEmpty()) {
            List<SectionDto> sectionDtoList = survey.sections().stream()
                .map(section -> new SectionDto(
                    null,
                    section.title(),
                    section.description(),
                    section.order(),
                    section.nextSectionOrder()
                ))
                .toList();

            questionCommand.upsertSections(surveyId, sectionDtoList);
            log.info("[FormRequestLambda] 섹션 생성 완료 - surveyId: {}, sectionCount: {}", surveyId, sectionDtoList.size());
        }

        // 3. 문항 생성
        List<QuestionUpsertDto.UpsertInfo> questionUpsertInfoList = new ArrayList<>();
        AtomicInteger questionOrder = new AtomicInteger(0);

        if (survey.sections() != null) {
            for (FormConversionResponse.Section section : survey.sections()) {
                if (section.questions() != null) {
                    for (FormConversionResponse.Question question : section.questions()) {
                        QuestionType questionType = mapQuestionType(question.type());
                        if (questionType == null) {
                            log.warn("[FormRequestLambda] 지원하지 않는 문항 타입 - type: {}, title: {}",
                                question.type(), question.title());
                            continue;
                        }

                        QuestionUpsertDto.UpsertInfo.UpsertInfoBuilder builder = QuestionUpsertDto.UpsertInfo.builder()
                            .questionId(null)
                            .title(question.title())
                            .description(question.description())
                            .isRequired(question.required())
                            .questionType(questionType)
                            .questionOrder(questionOrder.getAndIncrement())
                            .section(section.order())
                            .nextSection(section.nextSectionOrder());

                        // Choice 타입인 경우 옵션 설정
                        if (questionType == QuestionType.CHOICE && question.options() != null) {
                            boolean hasOtherOption = question.options().stream()
                                .anyMatch(FormConversionResponse.Option::isOther);
                            boolean isSectionDecidable = question.options().stream()
                                .anyMatch(opt -> opt.goToSectionOrder() != null);

                            builder.maxChoice(1)
                                .hasNoneOption(false)
                                .hasCustomInput(hasOtherOption)
                                .isSectionDecidable(isSectionDecidable)
                                .options(question.options().stream()
                                    .map(opt -> OptionDto.builder()
                                        .content(opt.text())
                                        .nextSection(opt.goToSectionOrder())
                                        .build())
                                    .toList());
                        }

                        questionUpsertInfoList.add(builder.build());
                    }
                }
            }
        }

        if (!questionUpsertInfoList.isEmpty()) {
            QuestionUpsertDto questionUpsertDto = QuestionUpsertDto.builder()
                .surveyId(surveyId)
                .upsertInfoList(questionUpsertInfoList)
                .build();

            QuestionUpsertDto savedQuestions = questionCommand.upsertQuestionList(questionUpsertDto);
            log.info("[FormRequestLambda] 문항 생성 완료 - surveyId: {}, questionCount: {}",
                surveyId, savedQuestions.getUpsertInfoList().size());

            // 4. Choice 문항의 옵션 저장
            List<OptionUpsertDto> optionUpsertDtoList = new ArrayList<>();
            List<QuestionUpsertDto.UpsertInfo> savedInfoList = savedQuestions.getUpsertInfoList();

            for (int i = 0; i < savedInfoList.size(); i++) {
                QuestionUpsertDto.UpsertInfo savedInfo = savedInfoList.get(i);
                QuestionUpsertDto.UpsertInfo originalInfo = questionUpsertInfoList.get(i);

                if (savedInfo.getQuestionType() == QuestionType.CHOICE && originalInfo.getOptions() != null) {
                    List<OptionDto> options = originalInfo.getOptions().stream()
                        .map(opt -> OptionDto.builder()
                            .questionId(savedInfo.getQuestionId())
                            .content(opt.getContent())
                            .nextSection(opt.getNextSection())
                            .build())
                        .toList();

                    optionUpsertDtoList.add(OptionUpsertDto.builder()
                        .questionId(savedInfo.getQuestionId())
                        .optionInfoList(options)
                        .build());
                }
            }

            if (!optionUpsertDtoList.isEmpty()) {
                questionCommand.upsertChoiceOptionList(optionUpsertDtoList);
                log.info("[FormRequestLambda] 옵션 생성 완료 - surveyId: {}, choiceQuestionCount: {}",
                    surveyId, optionUpsertDtoList.size());
            }
        }

        return surveyId;
    }

    private QuestionType mapQuestionType(String googleFormType) {
        if (googleFormType == null) {
            return null;
        }

        return switch (googleFormType.toUpperCase()) {
            case "MULTIPLE_CHOICE", "CHECKBOX", "DROPDOWN", "LIST" -> QuestionType.CHOICE;
            case "SHORT_ANSWER", "SHORT" -> QuestionType.SHORT;
            case "PARAGRAPH", "LONG" -> QuestionType.LONG;
            case "LINEAR_SCALE", "SCALE" -> QuestionType.RATING;
            case "DATE" -> QuestionType.DATE;
            case "NUMBER" -> QuestionType.NUMBER;
            default -> {
                log.warn("[FormRequestLambda] 알 수 없는 문항 타입 - type: {}", googleFormType);
                yield null;
            }
        };
    }
}
