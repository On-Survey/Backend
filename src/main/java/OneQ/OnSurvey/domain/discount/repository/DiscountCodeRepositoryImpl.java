package OneQ.OnSurvey.domain.discount.repository;

import OneQ.OnSurvey.domain.discount.entity.DiscountCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DiscountCodeRepositoryImpl implements DiscountCodeRepository {

    private final DiscountCodeJpaRepository jpaRepository;

    @Override
    public DiscountCode save(DiscountCode discountCode) {
        return jpaRepository.save(discountCode);
    }

    @Override
    public Optional<DiscountCode> findByCode(String code) {
        return jpaRepository.findByCode(code);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public List<DiscountCode> findAll() {
        return jpaRepository.findAll();
    }
}
