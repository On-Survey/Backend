package OneQ.OnSurvey.domain.discount.service;

import OneQ.OnSurvey.global.common.exception.CustomException;
import OneQ.OnSurvey.domain.discount.DiscountCodeErrorCode;
import OneQ.OnSurvey.domain.discount.entity.DiscountCode;
import OneQ.OnSurvey.domain.discount.model.response.DiscountCodeResponse;
import OneQ.OnSurvey.domain.discount.model.response.ValidateDiscountCodeResponse;
import OneQ.OnSurvey.domain.discount.repository.DiscountCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiscountCodeQueryService {

    private final DiscountCodeRepository discountCodeRepository;

    /** 코드 존재 여부만 확인 */
    public ValidateDiscountCodeResponse validate(String code) {
        DiscountCode discountCode = discountCodeRepository.findByCode(code)
                .orElseThrow(() -> new CustomException(DiscountCodeErrorCode.DISCOUNT_CODE_NOT_FOUND));
        validateNotExpired(discountCode);
        return new ValidateDiscountCodeResponse(true);
    }

    /** 설문 등록 시 코드 검증 후 엔티티 반환 */
    public DiscountCode getByCode(String code) {
        DiscountCode discountCode = discountCodeRepository.findByCode(code)
                .orElseThrow(() -> new CustomException(DiscountCodeErrorCode.DISCOUNT_CODE_NOT_FOUND));
        validateNotExpired(discountCode);
        return discountCode;
    }

    public List<DiscountCodeResponse> findAll() {
        return discountCodeRepository.findAll().stream()
                .map(DiscountCodeResponse::from)
                .toList();
    }

    private void validateNotExpired(DiscountCode discountCode) {
        if (discountCode.getExpiredAt().isBefore(LocalDate.now())) {
            throw new CustomException(DiscountCodeErrorCode.DISCOUNT_CODE_EXPIRED);
        }
    }
}
