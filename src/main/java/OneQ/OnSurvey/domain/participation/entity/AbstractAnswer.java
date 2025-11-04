package OneQ.OnSurvey.domain.participation.entity;

import OneQ.OnSurvey.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter @SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity @Table(name = "answer")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AbstractAnswer extends BaseEntity {
    @Id @Column(name = "answer_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long answerId;

    @Column(name = "member_id")
    private Long memberId;
}
