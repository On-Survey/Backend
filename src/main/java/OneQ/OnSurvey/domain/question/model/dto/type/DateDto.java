package OneQ.OnSurvey.domain.question.model.dto.type;

import OneQ.OnSurvey.domain.question.entity.question.Text;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter @SuperBuilder
public class DateDto extends DefaultQuestionDto {
    private LocalDateTime date;

    public static DateDto fromEntity(Text text) {
        return DateDto.builder()
            .date(text.getDefaultDate())
            .questionId(text.getQuestionId())
            .surveyId(text.getSurveyId())
            .questionType(text.getType().name())
            .title(text.getTitle())
            .description(text.getDescription())
            .isRequired(text.getIsRequired())
            .questionOrder(text.getOrder())
            .build();
    }
}
