package OneQ.OnSurvey.domain.survey.entity;

import OneQ.OnSurvey.domain.member.value.Interest;
import OneQ.OnSurvey.domain.survey.model.SurveyStatus;
import OneQ.OnSurvey.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

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

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime deadline;

    @Column(name = "is_temporary")
    @ColumnDefault("TRUE")
    @Builder.Default
    private Boolean isTemporary = true;

    @ColumnDefault("'WRITING'")
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

    @Column(name = "is_free", nullable = false)
    @ColumnDefault("FALSE")
    @Builder.Default
    private Boolean isFree = false;

    public static Survey of(
        Long memberId,
        String title,
        String description
    ) {
        return Survey.builder()
            .memberId(memberId)
            .title(title)
            .description(description)
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
            LocalDateTime deadline,
            Integer totalCoin
    ) {
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.totalCoin = totalCoin;
    }

    public void updateInterests(Set<Interest> interests) {
        this.interests.clear();
        this.interests.addAll(interests);
    }

    public void markFree() {
        this.isFree = true;
    }

    public void changeOwner(Long newMemberId) {
        this.memberId = newMemberId;
    }
}
