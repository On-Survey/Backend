package OneQ.OnSurvey.domain.question.model.dto.type;

import OneQ.OnSurvey.domain.question.entity.question.DateAnswer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter @SuperBuilder @ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DateDto extends DefaultQuestionDto {
    private LocalDateTime date;

    public static DateDto fromEntity(DateAnswer date) {
        return DateDto.builder()
            .date(date.getDefaultDate())
            .questionId(date.getQuestionId())
            .surveyId(date.getSurveyId())
            .questionType(date.getType())
            .title(date.getTitle())
            .description(date.getDescription())
            .isRequired(date.getIsRequired())
            .questionOrder(date.getOrder())
            .section(date.getSection() != null ? date.getSection() : 1)
            .imageUrl(date.getImageUrl())
            .build();
    }

    @Schema(description = "문항 타입 유형", example = "DATE")
    @Override
    public String getQuestionType() {
        return super.getQuestionType();
    }
}
