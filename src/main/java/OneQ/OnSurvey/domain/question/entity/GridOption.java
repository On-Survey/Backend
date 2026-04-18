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

    @Column
    private String content;

    @Column
    private Integer order;
}
