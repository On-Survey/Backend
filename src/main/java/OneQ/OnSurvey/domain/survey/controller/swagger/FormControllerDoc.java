package OneQ.OnSurvey.domain.survey.controller.swagger;

import OneQ.OnSurvey.domain.survey.model.request.QuestionRequest;
import OneQ.OnSurvey.domain.survey.model.response.CreateQuestionResponse;
import OneQ.OnSurvey.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface FormControllerDoc {

    @PostMapping("surveys/{surveyId}/questions")
    @Operation(summary = "새로운 문항을 생성합니다.")
    @ApiResponse(responseCode = "200", description = "문항 생성 성공",
        content = @Content(
            schema = @Schema(implementation = CreateQuestionResponse.class),
            examples = @ExampleObject(
                value = """
                    {
                        "code": 1,
                        "message": "성공하였습니다.",
                        "result": {
                            "surveyId": 2,
                            "questionId": 101,
                            "order": 2,
                            "title": "string",
                            "type": "LONG"
                        },
                        "success": true
                    }
                """
            )
        )
    )
    SuccessResponse<CreateQuestionResponse> createQuestion(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "생성할 문항 단건",
            required = true,
            content = @Content(
                schema = @Schema(implementation = QuestionRequest.class),
                examples = @ExampleObject(
                    name = "문항 생성",
                    value = """
                        {
                            "info": [
                                {
                                    "questionType": "LONG",
                                    "questions": [
                                        {
                                           "type": "LONG",
                                           "title": "string",
                                           "questionOrder": 2
                                        }
                                    ]
                                }
                            ]
                        }
                    """
                )
            )
        )
        @RequestBody QuestionRequest request,
        @PathVariable Long surveyId
    );
}
