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
import OneQ.OnSurvey.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                )
                .build();

        upsertDto = questionCommand.upsertQuestionList(upsertDto);
        return CreateQuestionResponse.fromDto(upsertDto);
    }

    @Override
    public UpdateQuestionResponse upsertQuestions(Long surveyId, QuestionRequest request) {
        log.info("[FORM:updateSurvey] 문항 임시저장: surveyId: {}, request: {}", surveyId, request.toString());

        if (request.getQuestions().isEmpty()) {
            log.warn("[FORM:updateSurvey] 문항 데이터가 비어있습니다.");
            throw new CustomException(SurveyErrorCode.SURVEY_FORM_EMPTY_REQUEST);
        }
        if (request.getQuestions().stream().anyMatch(dto -> dto.getQuestionType() == null)) {
            log.warn("[FORM:updateSurvey] 문항 타입이 유효하지 않습니다.");
            throw new CustomException(SurveyErrorCode.SURVEY_FORM_INVALID_QUESTION_TYPE);
        }

        // questionUpsertDto : 원본 문항 정보
        QuestionUpsertDto questionUpsertDto =
                QuestionConverter.toQuestionUpsertDto(surveyId, request.getQuestions());

        // CHOICE 타입에 대한 questionID - UpsertInfo 맵 생성
        Map<Long, QuestionUpsertDto.UpsertInfo> questionIdUpsertInfoListMap =
                questionUpsertDto.getUpsertInfoList().stream()
                        .filter(info -> QuestionType.CHOICE.equals(info.getQuestionType()))
                        .collect(Collectors.toMap(
                                QuestionUpsertDto.UpsertInfo::getQuestionId,
                                Function.identity()
                        ));

        log.info("[FORM:updateSurvey] Choice 문항 맵: {}", questionIdUpsertInfoListMap);

        // 문항 UPSERT
        questionUpsertDto = questionCommand.upsertQuestionList(questionUpsertDto);

        // 보기 UPSERT DTO 생성
        List<OptionUpsertDto> optionUpsertDtoList = questionIdUpsertInfoListMap.entrySet().stream()
                .map(entry -> OptionUpsertDto.builder()
                        .questionId(entry.getKey())
                        .optionInfoList(entry.getValue().getOptions())
                        .build())
                .toList();

        log.info("[FORM:updateSurvey] 문항 별 보기 리스트: {}", optionUpsertDtoList);

        // 보기 UPSERT
        optionUpsertDtoList = questionCommand.upsertChoiceOptionList(optionUpsertDtoList);

        Map<Long, OptionUpsertDto> optionDtoMap = optionUpsertDtoList.stream()
                .collect(Collectors.toMap(
                        OptionUpsertDto::getQuestionId,
                        Function.identity()
                ));

        // UPSERT 결과를 questionUpsertDto에 다시 매핑
        questionUpsertDto.getUpsertInfoList().forEach(upsertInfo -> {
            Long questionId = upsertInfo.getQuestionId();
            OptionUpsertDto optionInfoList = optionDtoMap.get(questionId);

            if (optionInfoList != null) {
                upsertInfo.setOptions(optionInfoList.getOptionInfoList());
            }
        });

        return new UpdateQuestionResponse(
                questionUpsertDto.getSurveyId(),
                questionUpsertDto.getUpsertInfoList()
        );
    }

    @Override
    public SurveyFormResponse completeSurvey(Long userKey, Long surveyId, SurveyFormRequest request) {
        log.info("[FORM:completeSurvey] 설문 제출 - surveyId: {}, userKey: {}", surveyId, userKey);
        return surveyCommand.submitSurvey(userKey, surveyId, request);
    }

    @Override
    public InterestResponse updateInterest(Long surveyId, SurveyInterestRequest request) {
        log.info("[FORM:updateInterest] surveyId: {}, interests: {}", surveyId, request.getInterests());
        return surveyCommand.upsertInterest(surveyId, request.getInterests());
    }

    @Override
    public ScreeningResponse createScreening(Long surveyId, ScreeningRequest request) {
        log.info("[FORM:createScreening] surveyId: {}, content: {}", surveyId, request.content());
        return surveyCommand.upsertScreening(null, surveyId, request.content(), request.answer());
    }
}
