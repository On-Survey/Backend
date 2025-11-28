package OneQ.OnSurvey.domain.participation.entity;

import OneQ.OnSurvey.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
@SuperBuilder
public abstract class AbstractAnswer extends BaseEntity {

    @Id @Column(name = "answer_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long answerId;

    @Column(name = "member_id")
    protected Long memberId;
}
