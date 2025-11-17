package OneQ.OnSurvey.domain.survey.controller;

import OneQ.OnSurvey.domain.member.service.MemberFinder;
import OneQ.OnSurvey.domain.participation.entity.ScreeningAnswer;
import OneQ.OnSurvey.domain.participation.model.dto.AnswerInsertDto;
import OneQ.OnSurvey.domain.participation.service.answer.AnswerCommand;
import OneQ.OnSurvey.domain.participation.service.response.ResponseCommand;
import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;
import OneQ.OnSurvey.domain.question.service.QuestionQuery;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.domain.survey.model.request.InsertQuestionAnswerRequest;
import OneQ.OnSurvey.domain.survey.model.request.InsertScreeningAnswerRequest;
import OneQ.OnSurvey.domain.survey.model.response.ParticipationQuestionResponse;
import OneQ.OnSurvey.domain.survey.model.response.SurveyParticipationResponse;
import OneQ.OnSurvey.domain.survey.model.response.ParticipationScreeningResponse;
import OneQ.OnSurvey.domain.survey.service.SurveyQuery;
import OneQ.OnSurvey.global.auth.custom.CustomUserDetails;
import OneQ.OnSurvey.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/survey-participation")
@RequiredArgsConstructor
public class ParticipationController {

    private final SurveyQuery surveyQueryService;
    private final QuestionQuery questionQueryService;

    private final AnswerCommand<ScreeningAnswer> answerCommand;

    private final MemberFinder memberFinder;
    private final ResponseCommand responseCommand;

    @GetMapping("surveys/ongoing")
    @Operation(summary = "노출 중인 설문을 조회합니다.")
    public SuccessResponse<SurveyParticipationResponse> getSurveyListOnGoing(
        @RequestParam(required = false, defaultValue = "0") Long lastSurveyId,
        @RequestParam(defaultValue = "15") Integer size
    ) {
        Pageable pageable = PageRequest.of(0, size, Sort.by("id"));
        return SuccessResponse.ok(
            surveyQueryService.getParticipationSurveyList(
                SurveyStatus.ONGOING, lastSurveyId, pageable
            )
        );
    }

    @GetMapping("surveys")
    @Operation(summary = "선택한 설문을 조회합니다.")
    public SuccessResponse<ParticipationQuestionResponse> getQuestionsOfSurveyId(
        @RequestParam Long surveyId
    ) {
        log.info("[PARTICIPATION] 응답하고자 하는 설문 문항조회 - surveyId: {}", surveyId);

        List<DefaultQuestionDto> questionDtoList = questionQueryService.getQuestionDtoListBySurveyId(surveyId);

        return SuccessResponse.ok(new ParticipationQuestionResponse(questionDtoList));
    }

    /* TODO 사용자 id 기반 관심사 필터링 추가 */
    @GetMapping("surveys/screenings")
    @Operation(summary = "관심사에 일치하는 설문의 스크리닝 문항을 조회합니다.")
    public SuccessResponse<ParticipationScreeningResponse> getRecommendedScreenings(
        @AuthenticationPrincipal CustomUserDetails details,
        @RequestParam(required = false, defaultValue = "0") Long lastSurveyId,
        @RequestParam(defaultValue = "5") Integer size
        ) {
        Long memberId = memberFinder.getMemberByUserKey(details.getUserKey()).getId();
        // memberId 기반 추천 설문id 조회

        Pageable pageable = PageRequest.of(0, size);
        return SuccessResponse.ok(surveyQueryService.getScreeningList(lastSurveyId, pageable));
    }

    @PostMapping("screenings/{screeningId}")
    @Operation(summary = "스크리닝 문항에 대한 응답을 생성합니다.")
    public SuccessResponse<Boolean> createScreeningAnswer(
        @AuthenticationPrincipal CustomUserDetails details,
        @RequestBody InsertScreeningAnswerRequest request,
        @PathVariable Long screeningId
    ) {
        Long memberId = memberFinder.getMemberByUserKey(details.getUserKey()).getId();

        log.info("[PARTICIPATION] 스크리닝 응답 생성 - screeningId: {}, userKey: {}, content: {}",
            screeningId, memberId, request.content());

        AnswerInsertDto.AnswerInfo answerInfo = AnswerInsertDto.AnswerInfo.builder()
            .id(screeningId)
            .memberId(memberId)
            .content(request.content())
            .build();
        return SuccessResponse.ok(answerCommand.insertAnswer(answerInfo));
    }

    @PostMapping("surveys/{surveyId}")
    @Operation(summary = "설문에 대한 응답을 생성합니다.")
    @Transactional
    public SuccessResponse<Boolean> createQuestionAnswer(
        @AuthenticationPrincipal CustomUserDetails details,
        @PathVariable Long surveyId,
        @RequestBody InsertQuestionAnswerRequest request
    ) {
        Long memberId = memberFinder.getMemberByUserKey(details.getUserKey()).getId();

        log.info("[PARTICIPATION] 설문 응답 생성 - surveyId: {}, userKey: {}, request: {}",
            surveyId, memberId, request.toString());

        AnswerInsertDto answerInsertDto = request.toDto(memberId);

        answerCommand.insertAnswers(answerInsertDto);
        responseCommand.createResponse(surveyId, memberId);

        return SuccessResponse.ok(true);

    }
}
