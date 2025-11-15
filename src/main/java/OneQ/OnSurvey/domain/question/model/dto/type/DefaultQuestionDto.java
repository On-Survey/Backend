package OneQ.OnSurvey.domain.question.model.dto.type;

import OneQ.OnSurvey.domain.question.entity.Question;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter @SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,             // 문자열로 식별
    include = JsonTypeInfo.As.PROPERTY,     // JSON 객체 내의 프로퍼티(필드)로 타입 식별
    property = "questionType",              // questionType 필드 사용
    defaultImpl = DefaultQuestionDto.class, // 일치하는 타입을 못찾으면 기본 dto 사용
    visible = true                          // questionType 필드를 dto 객체에도 바인딩
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ChoiceDto.class, name = "CHOICE"),
    @JsonSubTypes.Type(value = RatingDto.class, name = "RATING"),
    @JsonSubTypes.Type(value = DateDto.class, name = "DATE"),
})
public class DefaultQuestionDto {
    private Long questionId;
    private Long surveyId;

    @Schema(
        description = "문항 타입 유형",
        allowableValues = {
            "CHOICE", "RATING", "NPS", "SHORT", "LONG", "NUMBER", "DATE"
        }
    )
    private String questionType;
    private String title;
    private String description;
    private Boolean isRequired;
    private Integer questionOrder;

    public static DefaultQuestionDto fromEntity(Question question) {
        return DefaultQuestionDto.builder()
            .questionId(question.getQuestionId())
            .surveyId(question.getSurveyId())
            .questionType(question.getType().name())
            .title(question.getTitle())
            .description(question.getDescription())
            .isRequired(question.getIsRequired())
            .questionOrder(question.getOrder())
            .build();
    }
}
