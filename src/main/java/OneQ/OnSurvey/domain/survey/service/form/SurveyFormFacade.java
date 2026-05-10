package OneQ.OnSurvey.domain.survey.service.form;

import OneQ.OnSurvey.domain.question.model.QuestionType;
import OneQ.OnSurvey.domain.question.model.dto.GridOptionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.OptionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.QuestionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.SectionDto;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

        QuestionUpsertDto requestQuestionUpsertDto = QuestionConverter.toQuestionUpsertDto(surveyId, request.getQuestions());
        QuestionUpsertDto savedQuestionUpsertDto = questionCommand.upsertQuestionList(requestQuestionUpsertDto);

        OptionUpsertBundle optionUpsertBundle = buildOptionUpsertDtos(savedQuestionUpsertDto, requestQuestionUpsertDto);
        List<OptionUpsertDto> optionUpsertDtoList = optionUpsertBundle.choiceOptionUpsertDtos();
        List<GridOptionUpsertDto> gridOptionUpsertDtoList = optionUpsertBundle.gridOptionUpsertDtos();

        if (!optionUpsertDtoList.isEmpty()) {
            optionUpsertDtoList = questionCommand.upsertChoiceOptionList(optionUpsertDtoList);
        }
        if (!gridOptionUpsertDtoList.isEmpty()) {
            gridOptionUpsertDtoList = questionCommand.upsertGridOptionList(gridOptionUpsertDtoList);
        }
        Map<Long, OptionUpsertDto> optionDtoMap = mapOptionsByQuestionId(optionUpsertDtoList);
        Map<Long, GridOptionUpsertDto> gridOptionDtoMap = mapGridOptionsByQuestionId(gridOptionUpsertDtoList);

        applyOptionsToQuestionUpsertDto(savedQuestionUpsertDto, optionDtoMap, gridOptionDtoMap);

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

    @Override
    public SectionResponse upsertSection(Long surveyId, SectionRequest request) {
        log.info("[FORM:upsertSection] 섹션 수정 - surveyId: {}, size: {}", surveyId, request.sectionInfoList().size());
        List<SectionDto> result = questionCommand.upsertSections(surveyId, request.toDto());

        return SectionResponse.from(result);
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
                        .section(questionDto.getSection() != null ? questionDto.getSection() : 1)
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

    /** CHOICE/GRID 옵션 UPSERT DTO를 동시에 구성 */
    private OptionUpsertBundle buildOptionUpsertDtos(
        QuestionUpsertDto savedQuestionUpsertDto,
        QuestionUpsertDto requestedQuestionUpsertDto
    ) {
        Map<Integer, QuestionUpsertDto.UpsertInfo> requestedByOrder = requestedQuestionUpsertDto.getUpsertInfoList().stream()
            .filter(info -> info.getQuestionOrder() != null)
            .collect(Collectors.toMap(
                QuestionUpsertDto.UpsertInfo::getQuestionOrder,
                Function.identity(),
                (existing, replace) -> existing
            ));

        List<OptionUpsertDto> choiceOptionUpsertDtos = new ArrayList<>();
        List<GridOptionUpsertDto> gridOptionUpsertDtos = new ArrayList<>();

        savedQuestionUpsertDto.getUpsertInfoList().stream()
            .filter(savedInfo -> savedInfo.getQuestionType().isChoice() || savedInfo.getQuestionType().isGrid())
            .filter(savedInfo -> savedInfo.getQuestionId() != null && requestedByOrder.get(savedInfo.getQuestionOrder()) != null)
            .forEach(savedInfo -> {
                QuestionUpsertDto.UpsertInfo requestInfo = requestedByOrder.get(savedInfo.getQuestionOrder());

                if (savedInfo.getQuestionType().isChoice()) {
                    choiceOptionUpsertDtos.add(OptionUpsertDto.builder()
                        .questionId(savedInfo.getQuestionId())
                        .optionInfoList(requestInfo.getOptions() != null ? requestInfo.getOptions() : List.of())
                        .build()
                    );
                } else if (savedInfo.getQuestionType().isGrid()) {
                    gridOptionUpsertDtos.add(GridOptionUpsertDto.builder()
                        .questionId(savedInfo.getQuestionId())
                        .gridOptionInfoList(requestInfo.getGridOptions() != null ? requestInfo.getGridOptions() : List.of())
                        .build()
                    );
                }
            });

        return new OptionUpsertBundle(choiceOptionUpsertDtos, gridOptionUpsertDtos);
    }

    /** questionId 기준 OptionUpsertDto 맵핑 */
    private Map<Long, OptionUpsertDto> mapOptionsByQuestionId(List<OptionUpsertDto> optionUpsertDtoList) {
        return optionUpsertDtoList.stream()
            .collect(Collectors.toMap(
                OptionUpsertDto::getQuestionId,
                Function.identity()
            ));
    }

    /** questionId 기준 GridOptionUpsertDto 맵핑 */
    private Map<Long, GridOptionUpsertDto> mapGridOptionsByQuestionId(List<GridOptionUpsertDto> gridOptionUpsertDtoList) {
        return gridOptionUpsertDtoList.stream()
            .collect(Collectors.toMap(
                GridOptionUpsertDto::getQuestionId,
                Function.identity()
            ));
    }

    /** UPSERT된 보기 정보를 questionUpsertDto에 다시 반영 */
    private void applyOptionsToQuestionUpsertDto(
            QuestionUpsertDto questionUpsertDto,
            Map<Long, OptionUpsertDto> optionDtoMap,
            Map<Long, GridOptionUpsertDto> gridOptionDtoMap
    ) {
        questionUpsertDto.getUpsertInfoList().stream()
            .filter(upsertInfo -> upsertInfo.getQuestionType().isChoice() || upsertInfo.getQuestionType().isGrid())
            .forEach(upsertInfo -> {
                Long questionId = upsertInfo.getQuestionId();

                if (upsertInfo.getQuestionType().isChoice()) {
                    OptionUpsertDto optionInfoList = optionDtoMap.get(questionId);
                    if (optionInfoList != null) {
                        upsertInfo.updateOptions(optionInfoList.getOptionInfoList());
                    }
                } else if (upsertInfo.getQuestionType().isGrid()){
                    GridOptionUpsertDto gridOptionUpsertDto = gridOptionDtoMap.get(questionId);
                    if (gridOptionUpsertDto != null) {
                        upsertInfo.updateGridOptions(gridOptionUpsertDto.getGridOptionInfoList());
                    }
                }
        });
    }

    private record OptionUpsertBundle(
        List<OptionUpsertDto> choiceOptionUpsertDtos,
        List<GridOptionUpsertDto> gridOptionUpsertDtos
    ) { }
}