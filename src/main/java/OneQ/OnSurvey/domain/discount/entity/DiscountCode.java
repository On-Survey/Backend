package OneQ.OnSurvey.domain.discount.entity;

import OneQ.OnSurvey.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "discount_code")
public class DiscountCode extends BaseEntity {

    @Id
    @Column(name = "discount_code_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_name", nullable = false)
    private String organizationName;

    @Column(name = "code", nullable = false, unique = true, length = 6)
    private String code;

    @Column(name = "expired_at", nullable = false)
    private LocalDate expiredAt;

    public static DiscountCode of(String organizationName, String code, LocalDate expiredAt) {
        return DiscountCode.builder()
                .organizationName(organizationName)
                .code(code)
                .expiredAt(expiredAt)
                .build();
    }
}
