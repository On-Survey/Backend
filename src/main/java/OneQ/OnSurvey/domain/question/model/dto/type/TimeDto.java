package OneQ.OnSurvey.domain.question.model.dto.type;

import OneQ.OnSurvey.domain.question.entity.question.Time;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter @SuperBuilder @ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeDto extends DefaultQuestionDto {
    private Boolean isInterval;

    public static TimeDto fromEntity(Time time) {
        return TimeDto.builder()
            .isInterval(time.getIsInterval())
            .questionId(time.getQuestionId())
            .surveyId(time.getSurveyId())
            .questionType(time.getType())
            .title(time.getTitle())
            .description(time.getDescription())
            .isRequired(time.getIsRequired())
            .questionOrder(time.getOrder())
            .section(time.getSection() != null ? time.getSection() : 1)
            .imageUrl(time.getImageUrl())
            .build();
    }
}
