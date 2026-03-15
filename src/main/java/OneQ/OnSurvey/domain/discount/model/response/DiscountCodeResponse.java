package OneQ.OnSurvey.domain.discount.model.response;

import OneQ.OnSurvey.domain.discount.entity.DiscountCode;

import java.time.LocalDateTime;

public record DiscountCodeResponse(
        Long id,
        String organizationName,
        String code,
        LocalDateTime createdAt
) {
    public static DiscountCodeResponse from(DiscountCode entity) {
        return new DiscountCodeResponse(
                entity.getId(),
                entity.getOrganizationName(),
                entity.getCode(),
                entity.getCreatedAt()
        );
    }
}
