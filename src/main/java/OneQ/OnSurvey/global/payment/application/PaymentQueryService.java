package OneQ.OnSurvey.global.payment.application;

import OneQ.OnSurvey.global.infra.toss.common.dto.iap.PaymentSummaryResponse;
import OneQ.OnSurvey.global.payment.entity.Payment;
import OneQ.OnSurvey.global.payment.port.out.PaymentRepository;
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

