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

    private Integer dueCount;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private AgeRange age;

    @Enumerated(EnumType.STRING)
    private Residence residence;

    private Integer genderPrice;
    private Integer agePrice;
    private Integer residencePrice;
    private Integer dueCountPrice;

    @Builder.Default
    @Column(nullable = false)
    private boolean refundable = true;

    public static SurveyInfo createSurveyInfo(
            Long surveyId,
            Integer dueCount,
            Gender gender,
            AgeRange age,
            Residence residence,
            Integer genderPrice,
            Integer agePrice,
            Integer residencePrice,
            Integer dueCountPrice
    ) {
        return SurveyInfo.builder()
                .surveyId(surveyId)
                .dueCount(dueCount)
                .gender(gender)
                .age(age)
                .residence(residence)
                .genderPrice(genderPrice)
                .agePrice(agePrice)
                .residencePrice(residencePrice)
                .dueCountPrice(dueCountPrice)
                .refundable(true)
                .build();
    }

    public void updateSurveyInfo(
            Integer dueCount,
            Gender gender,
            AgeRange age,
            Residence residence,
            Integer genderPrice,
            Integer agePrice,
            Integer residencePrice,
            Integer dueCountPrice
    ) {
        this.dueCount = dueCount;
        this.gender = gender;
        this.age = age;
        this.residence = residence;
        this.genderPrice = genderPrice;
        this.agePrice = agePrice;
        this.residencePrice = residencePrice;
        this.dueCountPrice = dueCountPrice;
    }

    public void markNonRefundable() {
        this.refundable = false;
    }
}

