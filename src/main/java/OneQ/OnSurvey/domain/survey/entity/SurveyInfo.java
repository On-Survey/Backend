package OneQ.OnSurvey.domain.survey.entity;

import OneQ.OnSurvey.domain.survey.model.AgeRange;
import OneQ.OnSurvey.domain.survey.model.Gender;
import OneQ.OnSurvey.domain.survey.model.Residence;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

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

    @Builder.Default
    @Column(nullable = false)
    private Integer completedCount = 0;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @ElementCollection(targetClass = AgeRange.class)
    @CollectionTable(
            name = "survey_age",
            joinColumns = @JoinColumn(name = "info_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "age", length = 30, nullable = false)
    @Builder.Default
    private Set<AgeRange> ages = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private Residence residence;

    private Integer genderPrice;
    private Integer agePrice;
    private Integer residencePrice;
    private Integer dueCountPrice;

    private Integer promotionAmount;

    @Builder.Default
    @Column(nullable = false)
    private boolean refundable = true;

    public static SurveyInfo createSurveyInfo(
            Long surveyId,
            Integer dueCount,
            Gender gender,
            Set<AgeRange> ages,
            Residence residence,
            Integer genderPrice,
            Integer agePrice,
            Integer residencePrice,
            Integer dueCountPrice,
            Integer promotionAmount
    ) {
        return SurveyInfo.builder()
                .surveyId(surveyId)
                .dueCount(dueCount)
                .completedCount(0)
                .gender(gender)
                .ages(ages)
                .residence(residence)
                .genderPrice(genderPrice)
                .agePrice(agePrice)
                .residencePrice(residencePrice)
                .dueCountPrice(dueCountPrice)
                .promotionAmount(promotionAmount)
                .refundable(true)
                .build();
    }

    public void updateSurveyInfo(
            Integer dueCount,
            Gender gender,
            Set<AgeRange> ages,
            Residence residence,
            Integer genderPrice,
            Integer agePrice,
            Integer residencePrice,
            Integer dueCountPrice,
            Integer promotionAmount
    ) {
        this.dueCount = dueCount;
        this.gender = gender;
        this.ages = ages;
        this.residence = residence;
        this.genderPrice = genderPrice;
        this.agePrice = agePrice;
        this.residencePrice = residencePrice;
        this.dueCountPrice = dueCountPrice;
        this.promotionAmount = promotionAmount;
    }

    public void markNonRefundable() {
        this.refundable = false;
    }
}

