package OneQ.OnSurvey.domain.survey.controller;

import OneQ.OnSurvey.domain.member.service.MemberFinder;
import OneQ.OnSurvey.domain.question.model.QuestionType;
import OneQ.OnSurvey.domain.question.model.dto.OptionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.QuestionUpsertDto;
import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;
import OneQ.OnSurvey.domain.question.service.QuestionCommand;
import OneQ.OnSurvey.domain.question.service.QuestionConverter;
import OneQ.OnSurvey.domain.question.service.QuestionQuery;
import OneQ.OnSurvey.domain.survey.controller.swagger.FormControllerDoc;
import OneQ.OnSurvey.domain.survey.model.request.QuestionRequest;
import OneQ.OnSurvey.domain.survey.model.request.ScreeningRequest;
import OneQ.OnSurvey.domain.survey.model.request.SurveyFormRequest;
import OneQ.OnSurvey.domain.survey.model.response.CreateQuestionResponse;
import OneQ.OnSurvey.domain.survey.model.response.ScreeningResponse;
import OneQ.OnSurvey.domain.survey.model.response.SurveyFormResponse;
import OneQ.OnSurvey.domain.survey.model.response.UpdateQuestionResponse;
import OneQ.OnSurvey.domain.survey.service.SurveyCommand;
import OneQ.OnSurvey.global.auth.custom.CustomUserDetails;
import OneQ.OnSurvey.global.exception.CustomException;
import OneQ.OnSurvey.global.exception.ErrorCode;
import OneQ.OnSurvey.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/survey-form")
@RequiredArgsConstructor
public class FormController implements FormControllerDoc {
    private final SurveyCommand surveyCommand;
    private final QuestionCommand questionCommand;
    private final QuestionQuery questionQuery;

    private final MemberFinder memberFinder;

    @PostMapping("surveys")
    @Operation(summary = "설문 폼을 생성합니다.")
    public SuccessResponse<SurveyFormResponse> createSurvey(
        @AuthenticationPrincipal CustomUserDetails details,
        @RequestBody SurveyFormRequest request
    ) {
        Long memberId = memberFinder.getMemberByUserKey(details.getUserKey()).getId();

        SurveyFormResponse response = surveyCommand.upsertSurvey(
            null, request.title(), request.description(), memberId);

        return SuccessResponse.ok(response);
    }

    @Override
    @PostMapping("surveys/{surveyId}/questions")
    @Operation(summary = "새로운 문항을 생성합니다.")
    public SuccessResponse<CreateQuestionResponse> createQuestion(
        @RequestBody QuestionRequest request,
        @PathVariable Long surveyId
    ) {
        if (request.questions().isEmpty()
            || request.questions().getFirst().getQuestionType() == null
        ) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        DefaultQuestionDto questionDto = request.questions().getFirst();
        QuestionType type = QuestionType.valueOf(questionDto.getQuestionType());

        QuestionUpsertDto upsertDto = QuestionUpsertDto.builder()
            .surveyId(surveyId)
            .upsertInfoList(
                List.of(QuestionUpsertDto.UpsertInfo.builder()
                    .questionType(type)
                    .title(questionDto.getTitle())
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
        if (request.questions().isEmpty()
            || request.questions().getFirst().getQuestionType() == null
        ) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        QuestionUpsertDto questionUpsertDto =
            QuestionConverter.toQuestionUpsertDto(surveyId, request.questions());

        List<OptionUpsertDto> optionUpsertDtoList =
            questionUpsertDto.getUpsertInfoList().stream()
                .filter(info -> QuestionType.CHOICE.equals(info.getQuestionType()))
                .map(info -> OptionUpsertDto.builder()
                    .questionId(info.getQuestionId())
                    .optionInfoList(info.getOptions())
                    .build())
                .toList();
        questionUpsertDto = questionCommand.upsertQuestionList(questionUpsertDto);
        questionCommand.upsertChoiceOptionList(optionUpsertDtoList);

        return SuccessResponse.ok(new UpdateQuestionResponse(questionUpsertDto.getSurveyId(), questionUpsertDto.getUpsertInfoList()));
    }

    @PatchMapping("surveys/{surveyId}")
    @Operation(summary = "폼을 완성합니다.")
    public SuccessResponse<Boolean> completeSurvey(
        @PathVariable Long surveyId
    ) {
        return SuccessResponse.ok(surveyCommand.submitSurvey(surveyId));
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
