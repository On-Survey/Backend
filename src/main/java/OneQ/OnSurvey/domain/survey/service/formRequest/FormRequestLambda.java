package OneQ.OnSurvey.domain.survey.service.formRequest;

import OneQ.OnSurvey.domain.member.dto.MemberSearchResult;
import OneQ.OnSurvey.domain.member.service.MemberFinder;
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
import OneQ.OnSurvey.global.infra.discord.notifier.dto.SurveyConversionAlert;
import OneQ.OnSurvey.global.infra.transaction.TransactionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
@Qualifier("lambdaWebClient")
public class FormRequestLambda {

    private static final long FORM_CONVERSION_REQUEST_TIMEOUT = 20L;

    @Value("${external.lambda.survey-conversion.url:}")
    private String lambdaUrl;

    private final AlertNotifier alertNotifier;
    private final TransactionHandler transactionHandler;
    private final WebClient lambdaWebClient;

    private final MemberFinder memberFinder;
    private final FormUpdater formUpdater;
    private final SurveyCommand surveyCommand;
    private final QuestionCommand questionCommand;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void convertGoogleFormIntoSurvey(FormRequestConversionEvent event) {
        log.info("[FormRequestLambda] 구글폼 변환 시작 - requestId: {}, formUrl: {}", event.requestId(), event.formUrls());

        FormConversionPayload payload = new FormConversionPayload(event.formUrls());

        FormConversionResponse response = lambdaWebClient.post()
            .uri(lambdaUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(FormConversionResponse.class)
            .timeout(Duration.ofSeconds(FORM_CONVERSION_REQUEST_TIMEOUT))
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(3)))
            .doOnError(e -> {
                log.error("[FormRequestLambda] 구글폼 변환 중 오류 발생 - requestId: {}, error: {}", event.requestId(), e.getMessage(), e);
                alertNotifier.sendSurveyConversionAsync(
                    SurveyConversionAlert.error(1, 0, "구글폼 변환 중 오류가 발생했습니다. error: " + e.getMessage())
                );
                throw new CustomException(SurveyErrorCode.FORM_CONVERSION_FAILED);
            })
            .block();

        if (response == null || response.results() == null) {
            log.error("[FormRequestLambda] 구글폼 변환 실패 - 응답이 null입니다. requestId: {}", event.requestId());
            alertNotifier.sendSurveyConversionAsync(
                response == null
                    ? SurveyConversionAlert.error(1, 0, "변환 요청에 대한 응답이 null입니다.")
                    : SurveyConversionAlert.error(response.totalCount(), response.successCount(), response.error())
            );
            throw new CustomException(SurveyErrorCode.FORM_CONVERSION_FAILED);
        }

        SurveyConversionAlert alert = SurveyConversionAlert.success(
            response.totalCount(), response.successCount(), new ArrayList<>()
        );

        List<MemberSearchResult> memberResultList = memberFinder.searchMembers(event.email(), null, null, null);
        if (memberResultList.size() != 1) {
            log.warn("[FormRequestLambda] 설문 생성 실패 - 요청자 이메일로 회원을 찾을 수 없습니다. email: {}", event.email());
            throw new CustomException(SurveyErrorCode.FORM_REQUEST_NOT_FOUND);
        }
        Long memberId = memberResultList.getFirst().id();

        response.results().forEach(result -> {
            if (result.isSuccess()) {
                try {
                    alert.details().add(
                        transactionHandler.runInTransaction(() -> {
                            Long surveyId = createSurveyFromConversionResult(result, memberId);
                            formUpdater.markAsRegistered(event.requestId(), surveyId);

                            log.info("[FormRequestLambda] 구글폼 변환 성공 - requestId: {}, surveyId: {}", event.requestId(), surveyId);

                            if (result.unsupportedQuestions() != null && !result.unsupportedQuestions().isEmpty()) {
                                log.warn("[FormRequestLambda] 지원하지 않는 문항 존재 - requestId: {}, count: {}",
                                    event.requestId(), result.unsupportedQuestions().size());
                            }

                            return SurveyConversionAlert.SurveyDetails.success(
                                result.url(),
                                result.survey().title(),
                                surveyId,
                                memberId,
                                result.survey().sections() != null
                                    ? result.survey().sections().stream().mapToInt(s -> s.questions() != null
                                        ? s.questions().size()
                                        : 0).sum()
                                    : 0,
                                result.unsupportedQuestions() != null ? result.unsupportedQuestions().stream()
                                    .map(q -> new SurveyConversionAlert.SurveyDetails.UnsupportedQuestion(q.order(), q.type(), q.reason()))
                                    .toList() : List.of()
                            );
                        })
                    );
                } catch (Exception e) {
                    log.error("[FormRequestLambda] 설문 생성 중 오류 발생 - requestId: {}, error: {}",
                        event.requestId(), e.getMessage(), e);
                    alert.details().add(SurveyConversionAlert.SurveyDetails.failure(result.url(), "설문 생성 중 오류가 발생했습니다."));
                }
            } else {
                log.error("[FormRequestLambda] 구글폼 변환 실패 - requestId: {}, url: {}, message: {}",
                    event.requestId(), result.url(), result.message());
                alert.details().add(SurveyConversionAlert.SurveyDetails.failure(result.url(), result.message()));
            }
        });
        alertNotifier.sendSurveyConversionAsync(alert);
    }

    private Long createSurveyFromConversionResult(FormConversionResponse.Result result, Long memberId) {
        FormConversionResponse.Survey survey = result.survey();

        // 1. 설문 생성
        SurveyFormCreateRequest surveyRequest = new SurveyFormCreateRequest(
            survey.title(),
            survey.description()
        );
        SurveyFormResponse surveyResponse = surveyCommand.upsertSurvey(memberId, null, surveyRequest);
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
                    section.nextSectionOrder() != null ? section.nextSectionOrder() : 0
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
                            .imageUrl(question.imageUrl())
                            .section(section.order());

                        // Choice 타입인 경우 옵션 설정
                        if (questionType == QuestionType.CHOICE && question.options() != null) {
                            boolean hasOtherOption = question.options().stream()
                                .anyMatch(FormConversionResponse.Option::isOther);
                            boolean isSectionDecidable = question.options().stream()
                                .anyMatch(opt -> opt.goToSectionOrder() != null);
                            int maxChoice = switch (question.type().toUpperCase()) {
                                case "CHECKBOX" -> question.options().size(); // 체크박스는 여러 개 선택 가능
                                case "DROPDOWN", "MULTIPLE_CHOICE" -> 1; // 드롭다운, 객관식은 하나만 선택 가능
                                default -> 1; // 기본적으로 하나만 선택 가능하도록 설정
                            };

                            builder.maxChoice(maxChoice)
                                .hasNoneOption(false)
                                .hasCustomInput(hasOtherOption)
                                .isSectionDecidable(isSectionDecidable)
                                .options(question.options().stream()
                                    .filter(opt -> !opt.isOther())
                                    .map(opt -> OptionDto.builder()
                                        .content(opt.text())
                                        .nextSection(opt.goToSectionOrder())
                                        .imageUrl(opt.imageUrl())
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
            Map<Integer, QuestionUpsertDto.UpsertInfo> originalInfoMap = questionUpsertInfoList.stream()
                .collect(java.util.stream.Collectors.toMap(QuestionUpsertDto.UpsertInfo::getQuestionOrder, Function.identity()));

            for (QuestionUpsertDto.UpsertInfo savedInfo : savedInfoList) {
                QuestionUpsertDto.UpsertInfo originalInfo = originalInfoMap.get(savedInfo.getQuestionOrder());

                if (savedInfo.getQuestionType() == QuestionType.CHOICE && originalInfo.getOptions() != null) {
                    List<OptionDto> options = originalInfo.getOptions().stream()
                        .map(opt -> OptionDto.builder()
                            .questionId(savedInfo.getQuestionId())
                            .content(opt.getContent())
                            .nextSection(opt.getNextSection())
                            .imageUrl(opt.getImageUrl())
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
            case "MULTIPLE_CHOICE", "CHECKBOX" -> QuestionType.CHOICE;
            case "SHORT_ANSWER", "SHORT" -> QuestionType.SHORT;
            case "PARAGRAPH", "LONG" -> QuestionType.LONG;
            case "LINEAR_SCALE", "SCALE" -> QuestionType.RATING;
            case "DATE" -> QuestionType.DATE;
            case "NUMBER" -> QuestionType.NUMBER;
            case "IMAGE" -> QuestionType.IMAGE;
            default -> null;
        };
    }
}
