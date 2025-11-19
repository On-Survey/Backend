package OneQ.OnSurvey.domain.survey.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "survey_global_stats")
public class SurveyGlobalStats {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(nullable = false)
    @Builder.Default
    private Long totalDueCount = 1000L;

    @Column(nullable = false)
    @Builder.Default
    private Long totalCompletedCount = 1000L;

    @Column(nullable = false)
    @Builder.Default
    private Long totalPromotionCount = 1000L;

    public void increaseDueCount(long delta) {
        this.totalDueCount += delta;
    }

    public void increaseCompletedCount(long delta) {
        this.totalCompletedCount += delta;
    }

    public void increasePromotionCount(long delta) {
        this.totalPromotionCount += delta;
    }

    public static SurveyGlobalStats init() {
        return SurveyGlobalStats.builder()
                .id(1L)
                .totalDueCount(1000L)
                .totalCompletedCount(1000L)
                .totalPromotionCount(1000L)
                .build();
    }
}

