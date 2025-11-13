package OneQ.OnSurvey.domain.survey.model.request;

import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record QuestionRequest(
    @Schema(
        example = """
                [
                  {
                    "questionType": "CHOICE",
                    "questionId": 0,
                    "surveyId": 0,
                    "title": "string",
                    "description": "string",
                    "isRequired": true,
                    "questionOrder": 0,
                    "maxChoice": 1,
                    "hasNoneOption": false,
                    "hasCustomInput": false,
                    "options": [
                      {
                        "optionId": 1,
                        "content": "보기1",
                        "nextQuestionId": 2
                      },
                      {
                        "optionId": 2,
                        "content": "보기2",
                        "nextQuestionId": 3
                      }
                    ]
                  },
                  {
                    "questionType": "CHOICE",
                    "questionId": 1,
                    "surveyId": 0,
                    "title": "string",
                    "description": "string",
                    "isRequired": true,
                    "questionOrder": 1,
                    "maxChoice": 1,
                    "hasNoneOption": true,
                    "hasCustomInput": true,
                    "options": [
                      {
                        "optionId": 3,
                        "content": "보기1",
                        "nextQuestionId": 4
                      },
                      {
                        "optionId": 4,
                        "content": "보기2",
                        "nextQuestionId": 4
                      }
                    ]
                  },
                  {
                    "questionType": "SHORT",
                    "questionId": 2,
                    "surveyId": 0,
                    "title": "string",
                    "description": "string",
                    "isRequired": false,
                    "questionOrder": 2
                  },
                  {
                    "questionType": "RATING",
                    "questionId": 4,
                    "surveyId": 0,
                    "title": "string",
                    "description": "string",
                    "isRequired": false,
                    "questionOrder": 3,
                    "minValue": "최소 라벨",
                    "maxValue": "최대 라벨"
                  }
                ]
                """
    )
    List<DefaultQuestionDto> questions
) {
}
