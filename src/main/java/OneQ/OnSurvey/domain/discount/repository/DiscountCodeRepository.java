package OneQ.OnSurvey.domain.discount.repository;

import OneQ.OnSurvey.domain.discount.entity.DiscountCode;

import java.util.List;
import java.util.Optional;

public interface DiscountCodeRepository {
    DiscountCode save(DiscountCode discountCode);
    Optional<DiscountCode> findByCode(String code);
    boolean existsByCode(String code);
    List<DiscountCode> findAll();
}
