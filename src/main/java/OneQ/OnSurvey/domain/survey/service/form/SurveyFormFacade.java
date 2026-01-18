package OneQ.OnSurvey.domain.survey.service.form;

import OneQ.OnSurvey.domain.question.model.QuestionType;
import OneQ.OnSurvey.domain.question.model.dto.OptionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.QuestionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;
import OneQ.OnSurvey.domain.question.service.QuestionCommand;
import OneQ.OnSurvey.domain.question.service.QuestionConverter;
import OneQ.OnSurvey.domain.survey.SurveyErrorCode;
import OneQ.OnSurvey.domain.survey.model.request.*;
import OneQ.OnSurvey.domain.survey.model.response.*;
import OneQ.OnSurvey.domain.survey.service.command.SurveyCommand;
import OneQ.OnSurvey.global.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SurveyFormFacade implements SurveyFormUseCase {

    private final SurveyCommand surveyCommand;
    private final QuestionCommand questionCommand;

    @Override
    public SurveyFormResponse createSurvey(Long memberId, SurveyFormCreateRequest request) {
        log.info("[FORM] 설문 생성 - title: {}, description: {}, memberId: {}",
                request.title(), request.description(), memberId);

        return surveyCommand.upsertSurvey(memberId, null, request);
    }

    @Override
    public SurveyFormResponse updateSurveyDisplay(Long memberId, Long surveyId, SurveyFormCreateRequest request) {
        log.info("[FORM:updateSurvey] 설문 수정 - surveyId: {}, title: {}, description: {}, memberId: {}",
                surveyId, request.title(), request.description(), memberId);

        return surveyCommand.upsertSurvey(memberId, surveyId, request);
    }

    @Override
    public CreateQuestionResponse createQuestion(Long surveyId, QuestionRequest request) {
        log.info("[FORM:createQuestion] 새로운 문항 생성 - surveyId: {}, request: {}", surveyId, request.toString());

        validateCreateQuestionRequest(request);

        DefaultQuestionDto questionDto = request.getQuestions().getFirst();
        QuestionType type = parseQuestionType(questionDto);
        log.info("[FORM:createQuestion] 문항 타입: {}", type.name());

        QuestionUpsertDto upsertDto = buildSingleQuestionUpsertDto(surveyId, questionDto, type);

        upsertDto = questionCommand.upsertQuestionList(upsertDto);
        return CreateQuestionResponse.fromDto(upsertDto);
    }

    @Override
    public UpdateQuestionResponse upsertQuestions(Long surveyId, QuestionRequest request) {
        log.info("[FORM:updateSurvey] 문항 임시저장: surveyId: {}, request: {}", surveyId, request.toString());

        validateUpsertQuestionsRequest(request);

        QuestionUpsertDto requestQuestionUpsertDto =
                QuestionConverter.toQuestionUpsertDto(surveyId, request.getQuestions());

        QuestionUpsertDto savedQuestionUpsertDto =
                questionCommand.upsertQuestionList(requestQuestionUpsertDto);

        List<OptionUpsertDto> optionUpsertDtoList =
                buildOptionUpsertDtosFromSavedQuestions(savedQuestionUpsertDto, requestQuestionUpsertDto);
        log.info("[FORM:updateSurvey] 문항 별 보기 리스트: {}", optionUpsertDtoList);

        optionUpsertDtoList = questionCommand.upsertChoiceOptionList(optionUpsertDtoList);
        Map<Long, OptionUpsertDto> optionDtoMap = mapOptionsByQuestionId(optionUpsertDtoList);

        applyOptionsToQuestionUpsertDto(savedQuestionUpsertDto, optionDtoMap);

        return new UpdateQuestionResponse(
                savedQuestionUpsertDto.getSurveyId(),
                savedQuestionUpsertDto.getUpsertInfoList()
        );
    }

    @Override
    public SurveyFormResponse completeSurvey(Long userKey, Long surveyId, SurveyFormRequest request) {
        log.info("[FORM:completeSurvey] 설문 제출 - surveyId: {}, userKey: {}", surveyId, userKey);
        return surveyCommand.submitSurvey(userKey, surveyId, request);
    }

    @Override
    public SurveyFormResponse completeFreeSurvey(Long userKey, Long surveyId, FreeSurveyFormRequest request) {
        log.info("[FORM:completeFreeSurvey] 무료 설문 제출 - surveyId: {}, userKey: {}", surveyId, userKey);
        return surveyCommand.submitFreeSurvey(userKey, surveyId, request);
    }

    @Override
    public InterestResponse updateInterest(Long surveyId, SurveyInterestRequest request) {
        log.info("[FORM:updateInterest] surveyId: {}, interests: {}", surveyId, request.getInterests());
        return surveyCommand.upsertInterest(surveyId, request.getInterests());
    }

    @Override
    public ScreeningResponse createScreening(Long surveyId, ScreeningRequest request) {
        log.info("[FORM:createScreening] surveyId: {}, content: {}", surveyId, request.content());
        return surveyCommand.upsertScreening(surveyId, request.content(), request.answer());
    }


    private void validateCreateQuestionRequest(QuestionRequest request) {
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
    }

    private QuestionType parseQuestionType(DefaultQuestionDto questionDto) {
        String rawType = questionDto.getQuestionType();
        try {
            return QuestionType.valueOf(rawType);
        } catch (IllegalArgumentException e) {
            log.warn("[FORM:createQuestion] 지원하지 않는 문항 타입입니다. rawType={}", rawType);
            throw new CustomException(SurveyErrorCode.SURVEY_FORM_INVALID_QUESTION_TYPE);
        }
    }

    private QuestionUpsertDto buildSingleQuestionUpsertDto(
            Long surveyId,
            DefaultQuestionDto questionDto,
            QuestionType type
    ) {
        return QuestionUpsertDto.builder()
                .surveyId(surveyId)
                .upsertInfoList(
                        List.of(QuestionUpsertDto.UpsertInfo.builder()
                                .questionType(type)
                                .title(questionDto.getTitle())
                                .description(questionDto.getDescription())
                                .questionOrder(questionDto.getQuestionOrder())
                                .build())
                )
                .build();
    }

    private void validateUpsertQuestionsRequest(QuestionRequest request) {
        if (request.getQuestions().isEmpty()) {
            log.warn("[FORM:updateSurvey] 문항 데이터가 비어있습니다.");
            throw new CustomException(SurveyErrorCode.SURVEY_FORM_EMPTY_REQUEST);
        }
        if (request.getQuestions().stream().anyMatch(dto -> dto.getQuestionType() == null)) {
            log.warn("[FORM:updateSurvey] 문항 타입이 유효하지 않습니다.");
            throw new CustomException(SurveyErrorCode.SURVEY_FORM_INVALID_QUESTION_TYPE);
        }
    }

    /** UPSERT 이후 CHOICE 타입 문항에 대해 questionId -> UpsertInfo 맵 생성 */
    private Map<Long, QuestionUpsertDto.UpsertInfo> buildChoiceQuestionMap(QuestionUpsertDto questionUpsertDto) {
        return questionUpsertDto.getUpsertInfoList().stream()
                .filter(info -> QuestionType.CHOICE.equals(info.getQuestionType()))
                .filter(info -> info.getQuestionId() != null) // 🔹 null key 방지
                .collect(Collectors.toMap(
                        QuestionUpsertDto.UpsertInfo::getQuestionId,
                        Function.identity()
                ));
    }

    /** UPSERT된 QuestionUpsertDto에서 CHOICE 문항별 OptionUpsertDto 리스트 생성 */
    private List<OptionUpsertDto> buildOptionUpsertDtosFromSavedQuestions(
        QuestionUpsertDto savedQuestionUpsertDto,
        QuestionUpsertDto requestedQuestionUpsertDto
    ) {
        Set<Long> choiceQuestionIdSet = getChoiceQuestionIds(savedQuestionUpsertDto);
        Map<Long, QuestionUpsertDto.UpsertInfo> requestChoiceQuestionMap = buildChoiceQuestionMap(requestedQuestionUpsertDto);

        return choiceQuestionIdSet.stream()
            .map(qId -> OptionUpsertDto.builder()
                .questionId(qId)
                .optionInfoList(
                    (requestChoiceQuestionMap.get(qId) != null
                    && requestChoiceQuestionMap.get(qId).getOptions() != null)
                        ? requestChoiceQuestionMap.get(qId).getOptions()
                        : List.of()
                )
                .build())
            .toList();
    }

    private Set<Long> getChoiceQuestionIds(QuestionUpsertDto questionUpsertDto) {
        return questionUpsertDto.getUpsertInfoList().stream()
            .filter(info -> QuestionType.CHOICE.equals(info.getQuestionType()))
            .map(QuestionUpsertDto.UpsertInfo::getQuestionId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    /** questionId 기준 OptionUpsertDto 맵핑 */
    private Map<Long, OptionUpsertDto> mapOptionsByQuestionId(List<OptionUpsertDto> optionUpsertDtoList) {
        return optionUpsertDtoList.stream()
                .collect(Collectors.toMap(
                        OptionUpsertDto::getQuestionId,
                        Function.identity()
                ));
    }

    /** UPSERT된 보기 정보를 questionUpsertDto에 다시 반영 */
    private void applyOptionsToQuestionUpsertDto(
            QuestionUpsertDto questionUpsertDto,
            Map<Long, OptionUpsertDto> optionDtoMap
    ) {
        questionUpsertDto.getUpsertInfoList().forEach(upsertInfo -> {
            Long questionId = upsertInfo.getQuestionId();
            OptionUpsertDto optionInfoList = optionDtoMap.get(questionId);

            if (optionInfoList != null) {
                upsertInfo.setOptions(optionInfoList.getOptionInfoList());
            }
        });
    }
}