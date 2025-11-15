package OneQ.OnSurvey.domain.survey.entity;

import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.global.entity.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter @Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity @Table(name = "survey")
public class Survey extends BaseEntity {
    @Id @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id")
    private Long memberId;

    @Column(length = 32)
    private String title;

    @Column(length = 50)
    private String description;

    private LocalDateTime deadline;

    @Column(name = "is_temporary")
    @Builder.Default
    private Boolean isTemporary = true;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private SurveyStatus status = SurveyStatus.WRITING;

    private Integer totalCoin;

    @ElementCollection(targetClass = Interest.class)
    @CollectionTable(
        name = "survey_interest",
        joinColumns = @JoinColumn(name = "id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "interest", length = 30, nullable = false)
    @Builder.Default
    private Set<Interest> interests = new HashSet<>();

    public static Survey of(
        Long memberId,
        String title,
        String description,
        Integer totalCoin
    ) {
        return Survey.builder()
            .memberId(memberId)
            .title(title)
            .description(description)
            .totalCoin(totalCoin)
            .build();
    }

    public void submitSurvey() {
        this.isTemporary = false;
        this.status = SurveyStatus.ONGOING;
    }

    public void updateSurveyStatus(SurveyStatus status) {
        this.status = status;
    }

    public void updateSurvey(
            String title,
            String description,
            Integer totalCoin
    ) {
        this.title = title;
        this.description = description;
        this.totalCoin = totalCoin;
    }

    public void updateInterests(Set<Interest> interests) {
        this.interests = interests;
    }
}
