package OneQ.OnSurvey.domain.survey.model.request;

import OneQ.OnSurvey.domain.question.model.dto.type.QuestionTypeAndInfoDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record QuestionRequest(
    @Schema(
        description = "질문 타입별로 구분됩니다.",
        example = """
            [
              {
                "questionType": "CHOICE",
                "questions": [
                  {
                    "questionId": 0,
                    "surveyId": 0,
                    "type": "CHOICE",
                    "title": "string",
                    "description": "string",
                    "isRequired": true,
                    "questionOrder": 0
                  },
                  {
                    "questionId": 1,
                    "surveyId": 0,
                    "type": "CHOICE",
                    "title": "string",
                    "description": "string",
                    "isRequired": true,
                    "questionOrder": 0
                  }
                ]
              },
              {
                "questionType": "SHORT",
                "questions": [
                  {
                    "questionId": 2,
                    "surveyId": 0,
                    "type": "SHORT",
                    "title": "string",
                    "description": "string",
                    "isRequired": false,
                    "questionOrder": 0
                  }
                ]
              },
              {
                "questionType": "RATING",
                "questions": [
                  {
                    "questionId": 4,
                    "surveyId": 0,
                    "type": "RATING",
                    "title": "string",
                    "description": "string",
                    "isRequired": false,
                    "questionOrder": 0
                  }
                ]
              }
            ]
            """
    )
    List<QuestionTypeAndInfoDto> info
) {
}
