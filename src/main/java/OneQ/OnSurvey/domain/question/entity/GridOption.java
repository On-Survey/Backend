package OneQ.OnSurvey.domain.question.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Getter @Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity @Table(name = "GRID_OPTION")
public class GridOption {

    @Id @Column(name = "GRID_OPTION_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gridOptionId;

    @Column(name = "QUESTION_ID", nullable = false)
    private Long questionId;

    @Column(name = "IS_ROW", nullable = false)
    @ColumnDefault("FALSE")
    @Builder.Default
    private Boolean isRow = false;

    @Column(nullable = false)
    private String content;

    @Column(name = "GRID_ORDER", nullable = false)
    private Integer order;

    public static GridOption of(Long questionId, Boolean isRow, String content, Integer order) {
        return GridOption.builder()
            .questionId(questionId)
            .isRow(isRow)
            .content(content)
            .order(order)
            .build();
    }

    public void updateGridOption(String content, Integer order) {
        this.content = content;
        this.order = order;
    }
}
