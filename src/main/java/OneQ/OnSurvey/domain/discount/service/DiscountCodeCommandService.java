package OneQ.OnSurvey.domain.discount.service;

import OneQ.OnSurvey.domain.discount.entity.DiscountCode;
import OneQ.OnSurvey.domain.discount.model.request.CreateDiscountCodeRequest;
import OneQ.OnSurvey.domain.discount.model.response.DiscountCodeResponse;
import OneQ.OnSurvey.domain.discount.repository.DiscountCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DiscountCodeCommandService {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final DiscountCodeRepository discountCodeRepository;

    public DiscountCodeResponse create(CreateDiscountCodeRequest request) {
        String code = generateUniqueCode();
        DiscountCode discountCode = DiscountCode.of(request.organizationName(), code, request.expiredAt());
        discountCode = discountCodeRepository.save(discountCode);

        log.info("[DiscountCode:create] 할인 코드 생성 - org={}, code={}, expiredAt={}", request.organizationName(), code, request.expiredAt());

        return DiscountCodeResponse.from(discountCode);
    }

    private String generateUniqueCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
            }
            code = sb.toString();
        } while (discountCodeRepository.existsByCode(code));
        return code;
    }
}
