package OneQ.OnSurvey.domain.discount.service;

import OneQ.OnSurvey.domain.discount.entity.DiscountCode;
import OneQ.OnSurvey.domain.discount.model.request.CreateDiscountCodeRequest;
import OneQ.OnSurvey.domain.discount.model.response.DiscountCodeResponse;
import OneQ.OnSurvey.domain.discount.repository.DiscountCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DiscountCodeCommandService {

    private final DiscountCodeRepository discountCodeRepository;

    public DiscountCodeResponse create(CreateDiscountCodeRequest request) {
        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        DiscountCode discountCode = DiscountCode.of(request.organizationName(), code);
        discountCode = discountCodeRepository.save(discountCode);

        log.info("[DiscountCode:create] 할인 코드 생성 - org={}, code={}", request.organizationName(), code);

        return DiscountCodeResponse.from(discountCode);
    }
}
