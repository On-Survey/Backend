package OneQ.OnSurvey.domain.survey.controller;

import OneQ.OnSurvey.domain.participation.entity.QuestionAnswer;
import OneQ.OnSurvey.domain.participation.entity.ScreeningAnswer;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;
import OneQ.OnSurvey.domain.participation.service.answer.AnswerCommand;
import OneQ.OnSurvey.domain.participation.service.response.ResponseCommand;
import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;
import OneQ.OnSurvey.domain.question.service.QuestionQuery;
import OneQ.OnSurvey.domain.survey.SurveyErrorCode;
import OneQ.OnSurvey.domain.survey.entity.Survey;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.domain.survey.model.request.InsertQuestionAnswerRequest;
import OneQ.OnSurvey.domain.survey.model.request.InsertScreeningAnswerRequest;
import OneQ.OnSurvey.domain.survey.model.response.ParticipationQuestionResponse;
import OneQ.OnSurvey.domain.survey.model.response.ParticipationScreeningResponse;
import OneQ.OnSurvey.domain.survey.model.response.SurveyParticipationResponse;
import OneQ.OnSurvey.domain.survey.service.query.SurveyQuery;
import OneQ.OnSurvey.global.auth.custom.CustomUserDetails;
import OneQ.OnSurvey.global.common.exception.CustomException;
import OneQ.OnSurvey.global.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/survey-participation")
@RequiredArgsConstructor
public class ParticipationController {

    private final SurveyQuery surveyQueryService;
    private final QuestionQuery questionQueryService;
    private final AnswerCommand<ScreeningAnswer> answerCommand;
    private final AnswerCommand<QuestionAnswer> questionAnswerCommand;
    private final ResponseCommand responseCommand;

    @GetMapping("surveys/ongoing")
    @Operation(summary = "노출 중인 설문을 조회합니다.")
    public SuccessResponse<SurveyParticipationResponse> getSurveyListOnGoing(
        @AuthenticationPrincipal CustomUserDetails principal,
        @RequestParam(required = false, defaultValue = "0") Long lastSurveyId,
        @RequestParam(defaultValue = "15") Integer size
    ) {
        log.info("[PARTICIPATION] 노출 중 설문 조회 - lastSurveyId: {}, size: {}", lastSurveyId, size);

        Pageable recommendedPageable = PageRequest.of(0, size, Sort.by("id"));
        Pageable impendingPageable = PageRequest.of(0, size, Sort.by(
            Sort.Order.asc("deadline"),
            Sort.Order.asc("id")
        ));

        SurveyParticipationResponse.SliceSurveyData recommended = surveyQueryService.getParticipationSurveyList(
            lastSurveyId, recommendedPageable, SurveyStatus.ONGOING, principal.getMemberId(), principal.getUserKey()
        );
        SurveyParticipationResponse.SliceSurveyData impending = surveyQueryService.getParticipationSurveyList(
            lastSurveyId, LocalDateTime.now(), impendingPageable, SurveyStatus.ONGOING, principal.getMemberId(), principal.getUserKey()
        );

        SurveyParticipationResponse response = SurveyParticipationResponse.builder()
            .recommended(recommended.getSurveyDataList())
            .impending(impending.getSurveyDataList())
            .recommendedHasNext(recommended.getHasNext())
            .impendingHasNext(impending.getHasNext())
            .build();

        return SuccessResponse.ok(response);
    }

    @GetMapping("surveys/ongoing/recommended")
    @Operation(summary = "사용자 추천 설문을 조회합니다.")
    public SuccessResponse<SurveyParticipationResponse> getRecommendedSurveyList(
        @AuthenticationPrincipal CustomUserDetails principal,
        @RequestParam(required = false, defaultValue = "0") Long lastSurveyId,
        @RequestParam(defaultValue = "15") Integer size
    ) {
        log.info("[PARTICIPATION] 사용자 추천 설문 조회 - lastSurveyId: {}, size: {}", lastSurveyId, size);

        Pageable pageable = PageRequest.of(0, size, Sort.by("id"));
        SurveyParticipationResponse.SliceSurveyData recommended =
            surveyQueryService.getParticipationSurveyList(lastSurveyId, pageable, SurveyStatus.ONGOING, principal.getMemberId(), principal.getUserKey()
        );

        SurveyParticipationResponse response = SurveyParticipationResponse.builder()
            .recommended(recommended.getSurveyDataList())
            .recommendedHasNext(recommended.getHasNext())
            .build();

        return SuccessResponse.ok(response);
    }

    @GetMapping("surveys/ongoing/impending")
    @Operation(summary = "마감 임박 설문을 조회합니다.")
    public SuccessResponse<SurveyParticipationResponse> getImpendingSurveyList(
        @AuthenticationPrincipal CustomUserDetails principal,
        @RequestParam(required = false, defaultValue = "0") Long lastSurveyId,
        @RequestParam(required = false) LocalDateTime lastDeadline,
        @RequestParam(defaultValue = "15") Integer size
    ) {
        log.info("[PARTICIPATION] 마감 임박 설문 조회 - lastSurveyId: {}, lastDeadline: {}, size: {}", lastSurveyId, lastDeadline, size);

        Pageable pageable = PageRequest.of(0, size, Sort.by(
            Sort.Order.asc("deadline"),
            Sort.Order.asc("id")
        ));
        SurveyParticipationResponse.SliceSurveyData impending =
            surveyQueryService.getParticipationSurveyList(lastSurveyId, lastDeadline, pageable, SurveyStatus.ONGOING, principal.getMemberId(), principal.getUserKey()
        );

        SurveyParticipationResponse response = SurveyParticipationResponse.builder()
            .impending(impending.getSurveyDataList())
            .impendingHasNext(impending.getHasNext())
            .build();
        
        return SuccessResponse.ok(response);
    }

    @GetMapping("surveys")
    @Operation(summary = "선택한 설문을 조회합니다.")
    public SuccessResponse<ParticipationQuestionResponse> getQuestionsOfSurveyId(
        @RequestParam Long surveyId,
        @AuthenticationPrincipal CustomUserDetails principal
    ) {
        log.info("[PARTICIPATION] 응답하고자 하는 설문 문항조회 - surveyId: {}", surveyId);

        Survey survey = surveyQueryService.getSurveyById(surveyId);

        if (surveyQueryService.checkValidSegmentation(surveyId, principal.getUserKey())) {
            log.info("[PARTICIPATION] 세그먼트 불일치로 인한 설문 응답 불가 - surveyId: {}, userKey: {}", surveyId, principal.getUserKey());
            throw new CustomException(SurveyErrorCode.SURVEY_WRONG_SEGMENTATION);
        }

        List<DefaultQuestionDto> questionDtoList = questionQueryService.getQuestionDtoListBySurveyId(surveyId);

        ParticipationQuestionResponse body =
                ParticipationQuestionResponse.of(survey, questionDtoList);

        return SuccessResponse.ok(body);
    }

    @GetMapping("surveys/screenings")
    @Operation(summary = "세그멘테이션에 일치하는 설문의 스크리닝 문항을 조회합니다.")
    public SuccessResponse<ParticipationScreeningResponse> getRecommendedScreenings(
        @AuthenticationPrincipal CustomUserDetails principal,
        @RequestParam(required = false, defaultValue = "0") Long lastSurveyId,
        @RequestParam(defaultValue = "5") Integer size
    ) {
        log.info("[PARTICIPATION] 세그멘테이션 일치 스크리닝 문항 조회 - lastSurveyId: {}, size: {}", lastSurveyId, size);

        Pageable pageable = PageRequest.of(0, size);
        return SuccessResponse.ok(surveyQueryService.getScreeningList(lastSurveyId, pageable, principal.getMemberId(), principal.getUserKey()));
    }

    @PostMapping("screenings/{screeningId}")
    @Operation(summary = "스크리닝 문항에 대한 응답을 생성합니다.")
    public SuccessResponse<Boolean> createScreeningAnswer(
        @AuthenticationPrincipal CustomUserDetails principal,
        @RequestBody InsertScreeningAnswerRequest request,
        @PathVariable Long screeningId
    ) {
        log.info("[PARTICIPATION] 스크리닝 응답 생성 - screeningId: {}, userKey: {}, content: {}",
            screeningId, principal.getMemberId(), request.content());

        AnswerInsertDto.AnswerInfo answerInfo = AnswerInsertDto.AnswerInfo.builder()
            .id(screeningId)
            .memberId(principal.getMemberId())
            .content(request.content())
            .build();
        return SuccessResponse.ok(answerCommand.insertAnswer(answerInfo));
    }

    @PostMapping("surveys/{surveyId}")
    @Operation(summary = "설문에 대한 응답을 생성합니다.")
    public SuccessResponse<Boolean> createQuestionAnswer(
        @AuthenticationPrincipal CustomUserDetails principal,
        @PathVariable Long surveyId,
        @RequestBody InsertQuestionAnswerRequest request
    ) {
        log.info("[PARTICIPATION] 설문 응답 생성 - surveyId: {}, userKey: {}, request: {}",
            surveyId, principal.getMemberId(), request.toString());

        AnswerInsertDto answerInsertDto = request.toDto(principal.getMemberId());
        questionAnswerCommand.insertAnswers(answerInsertDto);

        return SuccessResponse.ok(true);
    }

    @PostMapping("surveys/{surveyId}/complete")
    @Operation(summary = "설문 작성을 완료합니다.")
    public SuccessResponse<Boolean> completeSurvey(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long surveyId
    ) {
        log.info("[PARTICIPATION] 설문 완료 - surveyId: {}, memberId: {}", surveyId, principal.getMemberId());

        Boolean result = responseCommand.createResponse(surveyId, principal.getMemberId());
        return SuccessResponse.ok(result);
    }
}
