package OneQ.OnSurvey.domain.participation.entity;

import OneQ.OnSurvey.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
public abstract class AbstractAnswer extends BaseEntity {

    @Id @Column(name = "answer_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long answerId;

    @Column(name = "member_id")
    protected Long memberId;

    protected void setMemberId(Long memberId) {
        this.memberId = memberId;
    }
}
