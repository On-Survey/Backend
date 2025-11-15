package OneQ.OnSurvey.domain.question.model.dto.type;

import OneQ.OnSurvey.domain.question.entity.question.DateAnswer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter @SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
            .build();
    }
}
