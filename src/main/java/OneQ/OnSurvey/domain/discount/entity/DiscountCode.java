package OneQ.OnSurvey.domain.discount.entity;

import OneQ.OnSurvey.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "code", nullable = false, unique = true, length = 16)
    private String code;

    public static DiscountCode of(String organizationName, String code) {
        return DiscountCode.builder()
                .organizationName(organizationName)
                .code(code)
                .build();
    }
}
