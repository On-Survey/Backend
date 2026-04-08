package OneQ.OnSurvey.domain.discount.repository;

import OneQ.OnSurvey.domain.discount.entity.DiscountCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiscountCodeJpaRepository extends JpaRepository<DiscountCode, Long> {
    Optional<DiscountCode> findByCode(String code);
    boolean existsByCode(String code);
}
