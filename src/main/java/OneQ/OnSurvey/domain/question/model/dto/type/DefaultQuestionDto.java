package OneQ.OnSurvey.domain.question.model.dto.type;

import OneQ.OnSurvey.domain.question.entity.Question;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter @SuperBuilder
public class DefaultQuestionDto {
    private Long questionId;
    private Long surveyId;
    private String type;
    private String title;
    private String description;
    private Boolean isRequired;
    private Integer questionOrder;

    public static DefaultQuestionDto fromEntity(Question question) {
        return DefaultQuestionDto.builder()
            .questionId(question.getQuestionId())
            .surveyId(question.getSurveyId())
            .type(question.getClass().toString())
            .title(question.getTitle())
            .description(question.getDescription())
            .isRequired(question.getIsRequired())
            .questionOrder(question.getOrder())
            .build();
    }
}
