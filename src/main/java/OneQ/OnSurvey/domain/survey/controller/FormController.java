package OneQ.OnSurvey.domain.survey.controller;

import OneQ.OnSurvey.domain.question.model.QuestionType;
import OneQ.OnSurvey.domain.question.model.dto.OptionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.QuestionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;
import OneQ.OnSurvey.domain.question.service.QuestionCommand;
import OneQ.OnSurvey.domain.question.service.QuestionConverter;
import OneQ.OnSurvey.domain.survey.SurveyErrorCode;
import OneQ.OnSurvey.domain.survey.controller.swagger.FormControllerDoc;
import OneQ.OnSurvey.domain.survey.model.request.*;
import OneQ.OnSurvey.domain.survey.model.response.*;
import OneQ.OnSurvey.domain.survey.service.SurveyCommand;
import OneQ.OnSurvey.global.auth.custom.CustomUserDetails;
import OneQ.OnSurvey.global.exception.CustomException;
import OneQ.OnSurvey.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/survey-form")
@RequiredArgsConstructor
@Slf4j
public class FormController implements FormControllerDoc {

    private final SurveyCommand surveyCommand;
    private final QuestionCommand questionCommand;

    @PostMapping("surveys")
    @Operation(summary = "설문 폼을 생성합니다.")
    public SuccessResponse<SurveyFormResponse> createSurvey(
        @AuthenticationPrincipal CustomUserDetails principal,
        @RequestBody SurveyFormCreateRequest request
    ) {
        log.info("[FORM] 설문 생성 - title: {}, description: {}, memberId: {}",
            request.title(), request.description(), principal.getMemberId());

        SurveyFormResponse response = surveyCommand.upsertSurvey(
                principal.getMemberId(), null, request
        );

        return SuccessResponse.ok(response);
    }

    @PatchMapping("surveys/{surveyId}/display")
    @Operation(summary = "설문 제목과 상세설명을 수정합니다.")
    public SuccessResponse<SurveyFormResponse> updateSurvey(
        @AuthenticationPrincipal CustomUserDetails principal,
        @RequestBody SurveyFormCreateRequest request,
        @PathVariable Long surveyId
    ) {
        log.info("[FORM:updateSurvey] 설문 수정 - surveyId: {}, title: {}, description: {}, memberId: {}",
            surveyId, request.title(), request.description(), principal.getMemberId());

        SurveyFormResponse response = surveyCommand.upsertSurvey(
            principal.getMemberId(), surveyId, request
        );

        return SuccessResponse.ok(response);
    }

    @Override
    @PostMapping("surveys/{surveyId}/questions")
    @Operation(summary = "새로운 문항을 생성합니다.")
    public SuccessResponse<CreateQuestionResponse> createQuestion(
        @RequestBody QuestionRequest request,
        @PathVariable Long surveyId
    ) {
        log.info("[FORM:createQuestion] 새로운 문항 생성 - surveyId: {}, request: {}", surveyId, request.toString());

        if (request.getQuestions().isEmpty()) {
            log.warn("[FORM:createQuestion] 문항 데이터가 비어있습니다.");
            throw new CustomException(SurveyErrorCode.SURVEY_FORM_EMPTY_REQUEST);
        }
        if (request.getQuestions().getFirst().getQuestionId() != null) {
            log.warn("[FORM:createQuestion] 문항 ID가 이미 존재합니다.");
            throw new CustomException(SurveyErrorCode.SURVEY_FORM_DUPLICATE_POST);
        }

        if (request.getQuestions().getFirst().getQuestionType() == null) {
            log.warn("[FORM:createQuestion] 문항 타입이 유효하지 않습니다.");
            throw new CustomException(SurveyErrorCode.SURVEY_FORM_INVALID_QUESTION_TYPE);
        }

        DefaultQuestionDto questionDto = request.getQuestions().getFirst();
        QuestionType type = QuestionType.valueOf(questionDto.getQuestionType());
        log.info("[FORM:createQuestion] 문항 타입: {}", type.name());

        QuestionUpsertDto upsertDto = QuestionUpsertDto.builder()
            .surveyId(surveyId)
            .upsertInfoList(
                List.of(QuestionUpsertDto.UpsertInfo.builder()
                    .questionType(type)
                    .title(questionDto.getTitle())
                    .description(questionDto.getDescription())
                    .questionOrder(questionDto.getQuestionOrder())
                    .build())
            ).build();

        upsertDto = questionCommand.upsertQuestionList(upsertDto);
        return SuccessResponse.ok(CreateQuestionResponse.fromDto(upsertDto));
    }

    @PutMapping("surveys/{surveyId}/questions")
    @Operation(summary = "기존 문항을 수정합니다. (임시저장)")
    @Transactional
    public SuccessResponse<UpdateQuestionResponse> updateSurvey(
        @RequestBody QuestionRequest request,
        @PathVariable Long surveyId
    ) {
        log.info("[FORM:updateSurvey] 문항 임시저장: surveyId: {}, request: {}", surveyId, request.toString());

        if (request.getQuestions().isEmpty()) {
            log.warn("[FORM:updateSurvey] 문항 데이터가 비어있습니다.");
            throw new CustomException(SurveyErrorCode.SURVEY_FORM_EMPTY_REQUEST);
        }
        if (request.getQuestions().stream().anyMatch(dto -> dto.getQuestionType() == null)) {
            log.warn("[FORM:updateSurvey] 문항 타입이 유효하지 않습니다.");
            throw new CustomException(SurveyErrorCode.SURVEY_FORM_INVALID_QUESTION_TYPE);
        }

        // questionUpserDto : 원본 문항 정보
        QuestionUpsertDto questionUpsertDto =
            QuestionConverter.toQuestionUpsertDto(surveyId, request.getQuestions());

        // CHOICE 타입에 대한 questionID - UpsertInfo 맵 생성
        Map<Long, QuestionUpsertDto.UpsertInfo> questionIdUpsertInfoListMap = questionUpsertDto.getUpsertInfoList().stream()
            .filter(info -> QuestionType.CHOICE.equals(info.getQuestionType()))
            .collect(Collectors.toMap(
                QuestionUpsertDto.UpsertInfo::getQuestionId,
                Function.identity()
            ));
        log.info("[FORM:updateSurvey] Choice 문항 맵: {}", questionIdUpsertInfoListMap);

        questionUpsertDto = questionCommand.upsertQuestionList(questionUpsertDto);

        List<OptionUpsertDto> optionUpsertDtoList = questionIdUpsertInfoListMap.entrySet().stream()
            .map(entry -> OptionUpsertDto.builder()
                .questionId(entry.getKey())
                .optionInfoList(entry.getValue().getOptions())
                .build())
            .toList();
        log.info("[FORM:updateSurvey] 문항 별 보기 리스트: {}", optionUpsertDtoList);

        optionUpsertDtoList = questionCommand.upsertChoiceOptionList(optionUpsertDtoList);
        Map<Long, OptionUpsertDto> optionDtoMap = optionUpsertDtoList.stream()
            .collect(Collectors.toMap(
                OptionUpsertDto::getQuestionId,
                Function.identity()
            ));

        questionUpsertDto.getUpsertInfoList().forEach(upsertInfo -> {
            Long questionId = upsertInfo.getQuestionId();
            OptionUpsertDto optionInfoList = optionDtoMap.get(questionId);

            if (optionInfoList != null) {
                upsertInfo.setOptions(optionInfoList.getOptionInfoList());
            }
        });

        return SuccessResponse.ok(new UpdateQuestionResponse(questionUpsertDto.getSurveyId(), questionUpsertDto.getUpsertInfoList()));
    }

    @PatchMapping("surveys/{surveyId}")
    @Operation(summary = "폼을 완성합니다.")
    public SuccessResponse<SurveyFormResponse> completeSurvey(
            @AuthenticationPrincipal CustomUserDetails details,
            @PathVariable Long surveyId,
            @RequestBody SurveyFormRequest request
    ) {
        return SuccessResponse.ok(surveyCommand.submitSurvey(details.getUserKey(), surveyId, request));
    }

    @PatchMapping("surveys/{surveyId}/interests")
    @Operation(summary = "설문의 관심사를 등록합니다.")
    public SuccessResponse<InterestResponse> updateInterest(
        @RequestBody SurveyInterestRequest request,
        @PathVariable Long surveyId
    ) {
        log.info("[FORM] surveyId: {}, interests: {}", surveyId, request.getInterests().toString());

        return SuccessResponse.ok(surveyCommand.upsertInterest(surveyId, request.getInterests()));
    }

    @PostMapping("surveys/{surveyId}/screenings")
    @Operation(summary = "설문에 대한 스크리닝 문항을 생성합니다.")
    public SuccessResponse<ScreeningResponse> createScreening(
        @RequestBody ScreeningRequest request,
        @PathVariable Long surveyId
    ) {
        return SuccessResponse.ok(surveyCommand.upsertScreening(null, surveyId, request.content(), request.answer()));
    }
}
