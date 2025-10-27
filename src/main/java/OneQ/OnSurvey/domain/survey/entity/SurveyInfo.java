package OneQ.OnSurvey.domain.survey.entity;

import OneQ.OnSurvey.domain.survey.model.AgeRange;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.Residence;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter @Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity @Table(name = "survey_info")
public class SurveyInfo {
    @Id @Column(name = "info_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "info_id")
    @SequenceGenerator(name = "info_id")
    private Long info_id;

    @Column(name = "survey_id")
    private Long surveyId;

    private LocalDateTime deadline;

    @Column(name = "due_count")
    private Integer dueCount;

    @Column(name = "question_count")
    private Integer questionCount;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private AgeRange age;

    @Enumerated(EnumType.STRING)
    private Residence residence;

    public SurveyInfo createSurveyInfo(
        Long surveyId,
        LocalDateTime deadLine,
        Integer dueCount,
        Integer questionCount,
        Gender gender,
        AgeRange age,
        Residence residence
    ) {
        return SurveyInfo.builder()
            .surveyId(surveyId)
            .deadline(deadLine)
            .dueCount(dueCount)
            .questionCount(questionCount)
            .gender(gender)
            .age(age)
            .residence(residence)
            .build();
    }
}

