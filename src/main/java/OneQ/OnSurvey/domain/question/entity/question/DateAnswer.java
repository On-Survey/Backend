package OneQ.OnSurvey.domain.question.entity.question;

import OneQ.OnSurvey.domain.question.entity.Question;
import OneQ.OnSurvey.domain.question.model.QuestionType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter @SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@DiscriminatorValue(value = QuestionType.Values.DATE)
public class DateAnswer extends Question {

    @Column(name = "default_date")
    private LocalDateTime defaultDate;

    public static DateAnswer of(
        Long surveyId,
        Integer order,
        String title,
        String description,
        Boolean isRequired,
        Integer section,
        LocalDateTime defaultDate,
        QuestionType type,
        String imageUrl
    ) {
        return DateAnswer.builder()
            .surveyId(surveyId)
            .order(order)
            .title(title)
            .description(description)
            .isRequired(isRequired)
            .section(section)
            .defaultDate(defaultDate)
            .type(type.name())
            .imageUrl(imageUrl)
            .build();
    }

    public void updateQuestion(
        String title,
        String description,
        Boolean isRequired,
        Integer order,
        Integer section,
        LocalDateTime defaultDate,
        String imageUrl
    ) {
        super.updateQuestion(title, description, isRequired, order, section, imageUrl);
        this.defaultDate = defaultDate;
    }
}
