package OneQ.OnSurvey.global.infra.toss.iap.service;

import OneQ.OnSurvey.global.infra.toss.iap.Payment;
import OneQ.OnSurvey.global.infra.toss.iap.dto.PaymentSummaryResponse;
import OneQ.OnSurvey.global.infra.toss.iap.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryService {

    private final PaymentRepository paymentRepository;

    public List<PaymentSummaryResponse> getPaymentsByUserKey(Long userKey) {
        List<Payment> payments =
                paymentRepository.findPaymentsByUserKeyOrderByDate(userKey);

        return payments.stream()
                .map(PaymentSummaryResponse::fromEntity)
                .toList();
    }
}

