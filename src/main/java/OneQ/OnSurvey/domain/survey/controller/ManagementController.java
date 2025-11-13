package OneQ.OnSurvey.domain.survey.controller;

import OneQ.OnSurvey.domain.member.service.MemberFinder;
import OneQ.OnSurvey.domain.participation.entity.QuestionAnswer;
import OneQ.OnSurvey.domain.participation.service.answer.AnswerQuery;
import OneQ.OnSurvey.domain.participation.service.response.ResponseQuery;
import OneQ.OnSurvey.domain.question.model.QuestionType;
import OneQ.OnSurvey.domain.question.service.QuestionQuery;
import OneQ.OnSurvey.domain.survey.model.response.SurveyManagementDetailResponse;
import OneQ.OnSurvey.domain.survey.model.response.SurveyManagementResponse;
import OneQ.OnSurvey.domain.survey.service.SurveyQuery;
import OneQ.OnSurvey.global.auth.custom.CustomUserDetails;
import OneQ.OnSurvey.global.exception.CustomException;
import OneQ.OnSurvey.global.exception.ErrorCode;
import OneQ.OnSurvey.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("v1/survey-management")
@RequiredArgsConstructor
public class ManagementController {
    private final SurveyQuery surveyQuery;
    private final QuestionQuery questionQuery;
    private final ResponseQuery responseQuery;
    private final AnswerQuery<QuestionAnswer> answerQuery;

    private final MemberFinder memberFinder;

    @GetMapping("surveys")
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

    @GetMapping("surveys/answers")
    @Operation(summary = "사용자가 관리할 설문을 상세 조회합니다.")
    public SuccessResponse<SurveyManagementDetailResponse> getSurveyManagementDetailInfo(
        @RequestParam Long surveyId,
        @AuthenticationPrincipal CustomUserDetails details
    ) {
        Long userKey = details.getUserKey();
        Long memberId = memberFinder.getMemberByUserKey(userKey).getId();

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
                    QuestionType.fromKey(dto.getType()),
                    dto.getTitle(),
                    dto.getDescription(),
                    dto.getIsRequired()
                )
            )
            .toList();

        List<Long> questionIdList = detailInfoList.stream().map(SurveyManagementDetailResponse.DetailInfo::getQuestionId).toList();
        detailInfoList = answerQuery.getDetailInfo(detailInfoList, questionIdList, memberId);

        response.updateDetailInfoList(detailInfoList);

        return SuccessResponse.ok(response);
    }
}
