package OneQ.OnSurvey.domain.survey.service.formRequest;

import OneQ.OnSurvey.domain.question.model.QuestionType;
import OneQ.OnSurvey.domain.question.model.dto.OptionDto;
import OneQ.OnSurvey.domain.question.model.dto.OptionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.QuestionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.SectionDto;
import OneQ.OnSurvey.domain.question.service.QuestionCommand;
import OneQ.OnSurvey.domain.survey.SurveyErrorCode;
import OneQ.OnSurvey.domain.survey.entity.FormRequest;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormConversionPayload;
import OneQ.OnSurvey.domain.survey.model.formRequest.FormConversionResponse;
import OneQ.OnSurvey.domain.survey.model.formRequest.event.FormRequestConversionEvent;
import OneQ.OnSurvey.domain.survey.model.request.SurveyFormCreateRequest;
import OneQ.OnSurvey.domain.survey.model.response.SurveyFormResponse;
import OneQ.OnSurvey.domain.survey.repository.formRequest.FormRequestRepository;
import OneQ.OnSurvey.domain.survey.service.command.SurveyCommand;
import OneQ.OnSurvey.global.common.exception.CustomException;
import OneQ.OnSurvey.global.infra.discord.notifier.AlertNotifier;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.SurveyConversionAlert;
import OneQ.OnSurvey.global.infra.transaction.TransactionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class FormEventListener {

    private final AlertNotifier alertNotifier;
    private final TransactionHandler transactionHandler;

    private final FormRequestLambda formRequestLambda;
    private final SurveyCommand surveyCommand;
    private final QuestionCommand questionCommand;

    private final FormRequestRepository formRequestRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void convertGoogleFormIntoSurvey(FormRequestConversionEvent event) {
        log.info("[FormEventListener] 구글폼 변환 시작 - requestId: {}, formUrl: {}", event.requestId(), event.formUrls());

        FormConversionPayload payload = new FormConversionPayload(event.requestId(), event.formUrls());
        FormConversionResponse response = formRequestLambda.convertGoogleFormIntoSurvey(payload);

        if (response == null || response.results() == null) {
            log.error("[FormRequestLambda] 구글폼 변환 실패 - 응답이 null입니다. requestId: {}", event.requestId());
            alertNotifier.sendSurveyConversionAsync(
                response == null
                    ? SurveyConversionAlert.error(1, 0, "변환 요청에 대한 응답이 null입니다.")
                    : SurveyConversionAlert.error(response.totalCount(), response.successCount(), response.error())
            );
            throw new CustomException(SurveyErrorCode.FORM_CONVERSION_FAILED);
        }

        List<SurveyConversionAlert.SurveyDetails> detailList = new ArrayList<>();
        response.results().forEach(result -> {
            if (result.isSuccess()) {
                try {
                    detailList.add(
                        transactionHandler.runInTransaction(() -> {
                            Long surveyId = createSurveyFromConversionResult(result, event.memberId());
                            int questionCount = result.survey().sections() != null
                                ? result.survey().sections().stream()
                                .mapToInt(s -> s.questions() != null ? s.questions().size() : 0)
                                .sum()
                                : 0;
                            FormRequest request = formRequestRepository.findById(event.requestId())
                                .orElseThrow(() -> new CustomException(SurveyErrorCode.FORM_REQUEST_NOT_FOUND));
                            request.markAsRegistered(surveyId, questionCount);

                            log.info("[FormRequestLambda] 구글폼 변환 성공 - requestId: {}, surveyId: {}, questionCount: {}", event.requestId(), surveyId, questionCount);
                            if (result.unsupportedQuestions() != null && !result.unsupportedQuestions().isEmpty()) {
                                log.warn("[FormRequestLambda] 지원하지 않는 문항 존재 - requestId: {}, count: {}",
                                    event.requestId(), result.unsupportedQuestions().size());
                            }

                            if (event.screening() != null) {
                                surveyCommand.upsertScreening(
                                    surveyId,
                                    event.screening().content(),
                                    event.screening().answer()
                                );
                            }
                            surveyCommand.submitSurvey(event.userKey(), surveyId, event.surveyForm());

                            return SurveyConversionAlert.SurveyDetails.success(
                                result.url(),
                                result.survey().title(),
                                surveyId,
                                event.memberId(),
                                questionCount,
                                result.unsupportedQuestions() != null ? result.unsupportedQuestions().stream()
                                    .map(q -> new SurveyConversionAlert.SurveyDetails.UnsupportedQuestion(q.order(), q.type(), q.reason()))
                                    .toList() : List.of()
                            );
                        })
                    );
                } catch (Exception e) {
                    log.error("[FormRequestLambda] 설문 생성 중 오류 발생 - requestId: {}, error: {}",
                        event.requestId(), e.getMessage(), e);
                    detailList.add(SurveyConversionAlert.SurveyDetails.failure(result.url(), "설문 생성 중 오류가 발생했습니다."));
                }
            } else {
                log.error("[FormRequestLambda] 구글폼 변환 실패 - requestId: {}, url: {}, message: {}",
                    event.requestId(), result.url(), result.message());
                detailList.add(SurveyConversionAlert.SurveyDetails.failure(result.url(), result.message()));
            }
        });
        alertNotifier.sendSurveyConversionAsync(
            SurveyConversionAlert.success(response.totalCount(), response.successCount(), detailList)
        );
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
