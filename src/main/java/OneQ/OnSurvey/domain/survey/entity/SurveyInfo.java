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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity @Table(name = "survey_info")
public class SurveyInfo {
    @Id @Column(name = "info_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long infoId;

    @Column(name = "survey_id")
    private Long surveyId;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private AgeRange age;

    @Enumerated(EnumType.STRING)
    private Residence residence;

    public static SurveyInfo createSurveyInfo(
            Long surveyId,
            Gender gender,
            AgeRange age,
            Residence residence
    ) {
        return SurveyInfo.builder()
                .surveyId(surveyId)
                .gender(gender)
                .age(age)
                .residence(residence)
                .build();
    }

    public void updateSurveyInfo(
            Gender gender,
            AgeRange age,
            Residence residence
    ) {
        this.gender = gender;
        this.age = age;
        this.residence = residence;
    }
}

