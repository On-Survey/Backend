package OneQ.OnSurvey.domain.survey.model.request;

import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter @ToString
public class QuestionRequest {
    @Schema(
        example = """
                [
                  {
                    "questionType": "CHOICE",
                    "questionId": 0,
                    "title": "string",
                    "description": "string",
                    "isRequired": true,
                    "section": 1,
                    "questionOrder": 0,
                    "maxChoice": 1,
                    "hasNoneOption": false,
                    "hasCustomInput": false,
                    "isSectionDecidable": true,
                    "options": [
                      {
                        "optionId": 1,
                        "content": "섹션2로 가는 보기",
                        "nextSection": 2
                      },
                      {
                        "optionId": 2,
                        "content": "섹션3으로 가는 보기",
                        "nextSection": 3
                      },
                      {
                        "optionId": 2,
                        "content": "설문을 끝내는 보기",
                        "nextSection": 0
                      },
                      {
                        "optionId": 2,
                        "content": "다음 섹션으로 가는 보기",
                        "nextSection": 2
                      }
                    ]
                  },
                  {
                    "questionType": "CHOICE",
                    "questionId": 1,
                    "title": "string",
                    "description": "string",
                    "isRequired": true,
                    "section": 1,
                    "questionOrder": 1,
                    "maxChoice": 1,
                    "hasNoneOption": true,
                    "hasCustomInput": true,
                    "isSectionDecidable": false,
                    "options": [
                      {
                        "optionId": 3,
                        "content": "보기1"
                      },
                      {
                        "optionId": 4,
                        "content": "보기2"
                      }
                    ]
                  },
                  {
                    "questionType": "SHORT",
                    "questionId": 2,
                    "title": "string",
                    "description": "string",
                    "isRequired": false,
                    "section": 2,
                    "questionOrder": 2
                  },
                  {
                    "questionType": "RATING",
                    "questionId": 4,
                    "title": "string",
                    "description": "string",
                    "isRequired": false,
                    "section": 3,
                    "questionOrder": 3,
                    "minValue": "최소 라벨",
                    "maxValue": "최대 라벨",
                    "rate": 5
                  }
                ]
                """,
        description = "새로운 보기는 optionId를 null로 설정"
    )
    List<DefaultQuestionDto> questions;
}
