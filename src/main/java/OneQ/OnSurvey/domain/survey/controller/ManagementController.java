package OneQ.OnSurvey.domain.survey.controller;

import OneQ.OnSurvey.domain.member.service.MemberFinder;
import OneQ.OnSurvey.domain.participation.entity.QuestionAnswer;
import OneQ.OnSurvey.domain.participation.service.answer.AnswerQuery;
import OneQ.OnSurvey.domain.participation.service.response.ResponseQuery;
import OneQ.OnSurvey.domain.question.model.QuestionType;
import OneQ.OnSurvey.domain.question.service.QuestionQuery;
import OneQ.OnSurvey.domain.survey.model.response.FormQuestionResponse;
import OneQ.OnSurvey.domain.survey.model.response.MySurveyListResponse;
import OneQ.OnSurvey.domain.survey.model.response.SurveyDetailResponse;
import OneQ.OnSurvey.domain.survey.model.response.SurveyManagementDetailResponse;
import OneQ.OnSurvey.domain.survey.model.response.SurveyManagementResponse;
import OneQ.OnSurvey.domain.survey.service.SurveyCommand;
import OneQ.OnSurvey.domain.survey.service.SurveyQuery;
import OneQ.OnSurvey.global.auth.custom.CustomUserDetails;
import OneQ.OnSurvey.global.exception.CustomException;
import OneQ.OnSurvey.global.exception.ErrorCode;
import OneQ.OnSurvey.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/survey-management")
@RequiredArgsConstructor
public class ManagementController {

    private final SurveyQuery surveyQuery;
    private final SurveyCommand surveyCommand;
    private final QuestionQuery questionQuery;
    private final ResponseQuery responseQuery;
    private final AnswerQuery<QuestionAnswer> answerQuery;

    private final MemberFinder memberFinder;

    @GetMapping("/surveys")
    @Operation(summary = "사용자가 생성한 설문을 조회합니다.")
    public SuccessResponse<SurveyManagementResponse> getSurveyManagementList(
        @AuthenticationPrincipal CustomUserDetails details
    ) {
        Long userKey = details.getUserKey();
        Long memberId = memberFinder.getMemberByUserKey(userKey).getId();

        List<SurveyManagementResponse.SurveyInfo> surveyInfoList = surveyQuery.getSurveyListByMemberId(memberId);

        surveyInfoList.forEach(info -> {
            int count = responseQuery.getResponseCountBySurveyId(info.getSurveyId());
            info.updateCurrentParticipationCount(count);
            }
        );
        return SuccessResponse.ok(new SurveyManagementResponse(surveyInfoList));
    }

    @GetMapping("/surveys/answers")
    @Operation(summary = "사용자가 응답을 확인할 설문을 상세 조회합니다.")
    public SuccessResponse<SurveyManagementDetailResponse> getSurveyManagementDetailInfo(
        @RequestParam Long surveyId,
        @AuthenticationPrincipal CustomUserDetails details
    ) {
        Long userKey = details.getUserKey();
        Long memberId = memberFinder.getMemberByUserKey(userKey).getId();

        log.info("[MANAGEMENT] 응답을 확인할 설문 상세조회 - surveyId: {}, memberId: {}", surveyId, memberId);

        SurveyManagementDetailResponse response = surveyQuery.getSurvey(surveyId);

        if (!memberId.equals(response.getMemberId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        int count = responseQuery.getResponseCountBySurveyId(surveyId);
        response.updateCurrentCount(count);

        List<SurveyManagementDetailResponse.DetailInfo> detailInfoList = questionQuery.getQuestionListBySurveyId(surveyId).info().stream()
            .map(dto -> new SurveyManagementDetailResponse.DetailInfo(
                    dto.getQuestionId(),
                    dto.getQuestionOrder(),
                    QuestionType.valueOf(dto.getQuestionType()),
                    dto.getTitle(),
                    dto.getDescription(),
                    dto.getIsRequired()
                )
            )
            .toList();

        detailInfoList = answerQuery.getDetailInfo(surveyId, detailInfoList);
        response.updateDetailInfoList(detailInfoList);

        return SuccessResponse.ok(response);
    }

    @GetMapping("/writing")
    @Operation(summary = "작성 중인 설문을 조회합니다.")
    public SuccessResponse<FormQuestionResponse> getQuestionsInWriting(
        @AuthenticationPrincipal CustomUserDetails principal,
        @RequestParam Long surveyId
    ) {
        Long userKey = principal.getUserKey();
        Long memberId = memberFinder.getMemberByUserKey(userKey).getId();

        log.info("[MANAGEMENT] 작성 중인 설문 조회 - surveyId: {}, memberId: {}", surveyId, memberId);

        FormQuestionResponse response = questionQuery.getWritingQuestions(surveyId);

        return SuccessResponse.ok(response);
    }

    @GetMapping
    @Operation(summary = "내 설문 목록 조회",
            description = "코인으로 결제한 설문들을 노출중/환불로 구분하여 조회합니다.")
    public SuccessResponse<MySurveyListResponse> getMySurveys(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long memberId = memberFinder.getMemberByUserKey(principal.getUserKey()).getId();
        return SuccessResponse.ok(surveyQuery.getMySurveys(memberId));
    }

    @GetMapping("/{surveyId}")
    @Operation(summary = "내 설문 결제 상세 조회",
            description = "선택한 설문의 코인 결제 및 타겟 정보를 조회합니다.")
    public SuccessResponse<SurveyDetailResponse> getMySurveyDetail(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long surveyId
    ) {
        Long memberId = memberFinder.getMemberByUserKey(principal.getUserKey()).getId();
        return SuccessResponse.ok(surveyQuery.getMySurveyDetail(memberId, surveyId));
    }

    @PostMapping("/{surveyId}/refund")
    @Operation(summary = "내 설문 결제 환불",
            description = "선택한 설문의 코인 결제를 환불하고, survey의 totalCoin 만큼 코인을 돌려줍니다.")
    public SuccessResponse<Boolean> refundMySurvey(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long surveyId
    ) {
        return SuccessResponse.ok(surveyCommand.refundSurvey(principal.getUserKey(), surveyId));
    }
}
