package OneQ.OnSurvey.domain.survey.model.formRequest;

import OneQ.OnSurvey.domain.question.model.dto.type.DefaultQuestionDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record FormValidationResponse(
    @Schema(
        description = "URL 검증이 이루어진 응답 결과",
        example = """
            [
                {
                    "url": "https://docs.google.com/forms/d/1Eq41ykgka_.../edit",
                    "message": null,
                    "totalCount": 30,
                    "convertible": 22,
                    "inconvertible": 8,
                    "inconvertibleDetails": [
                        {
                            "title": "비디오 문항 제목",
                            "type": "VIDEO",
                            "reason": "비디오 문항 미지원"
                        }, {
                            "title": "시간 문항 제목",
                            "type": "TIME",
                            "reason": "시간 문항 미지원"
                        }
                    ],
                    "convertibleDetails": [
                        {
                            "sectionTitle": "1. 기본 정보",
                            "sectionDescription": "1번 섹션",
                            "currSection": 1,
                            "nextSection": 2,
                            "info": [
                                {
                                    "questionType": "SHORT",
                                    "title": "단답형 문항 제목",
                                    "description": null,
                                    "isRequired": true,
                                    "questionOrder": 1,
                                    "section": 1,
                                    "imageUrl": null
                                }
                            ]
                        }, {
                            "sectionTitle": "2. 중간 정보",
                            "sectionDescription": "2번 섹션",
                            "currSection": 2,
                            "nextSection": 3,
                            "info": [
                                {
                                    "questionType": "DATE",
                                    "title": "날짜형 문항 제목",
                                    "description": null,
                                    "isRequired": true,
                                    "questionOrder": 2,
                                    "section": 2,
                                    "imageUrl": null
                                }
                            ]
                        }, {
                            "sectionTitle": "3. 마지막 정보",
                            "sectionDescription": "3번 섹션",
                            "currSection": 3,
                            "nextSection": 0,
                            "info": [
                                {
                                    "questionType": "RATING",
                                    "title": "평가형 문항 제목",
                                    "description": null,
                                    "isRequired": true,
                                    "questionOrder": 3,
                                    "section": 3,
                                    "imageUrl": null,
                                    "minValue": "최소라벨값",
                                    "maxValue": "최대라벨값",
                                    "rate": 6
                                }
                            ]
                        }
                    ]
                }, {
                    "url": "https://docs.google.com/forms/d/e/1Eq41ykgka_.../viewform",
                    "message": "설문 편집 권한이 부여되지 않았습니다."
                }, {
                    "url": "https://docs.google.com/forms/d/1Eq4gka_.../edit",
                    "message": "설문이 게시되지 않았습니다."
                }, {
                    "url": "https://docs.google.com/forms/d/1Eq4gka_.../edit",
                    "message": "변환된 설문 저장 중 에러가 발생했습니다. 재시도 해주세요."
                }
            ]
            """
    )
    List<Result> results,
    int emailSent
) {

    public record Result(
        String url,
        String message,

        int totalCount,
        int convertible,
        int inconvertible,
        List<Inconvertible> inconvertibleDetails,
        List<Convertible> convertibleDetails
    ) { }

    public record Inconvertible(
        String title,
        String type,
        String reason
    ) { }

    public record Convertible(
        String sectionTitle,
        String sectionDescription,
        Integer currSection,
        Integer nextSection,
        List<DefaultQuestionDto> info
    ) { }

    public static FormValidationResponse.Result success(
        String url,
        int count,
        int convertible,
        int inconvertible,
        List<Inconvertible> inconvertibleDetails,
        List<Convertible> convertibleDetails
    ) {
       return new FormValidationResponse.Result(url, null, count, convertible, inconvertible, inconvertibleDetails, convertibleDetails);
    }

    public static FormValidationResponse.Result fail(
        String url,
        String message
    ) {
        return new FormValidationResponse.Result(url, message, 0, 0, 0, List.of(), List.of());
    }
}
