package OneQ.OnSurvey.domain.survey.entity;

import OneQ.OnSurvey.domain.survey.model.AgeRange;
import OneQ.OnSurvey.domain.survey.model.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @Entity
@Table(name = "survey_info")
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

    private boolean residence; // 위 4개 항목 enum 으로 바꾸기
}

