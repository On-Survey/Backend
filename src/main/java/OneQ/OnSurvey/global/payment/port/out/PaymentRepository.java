package OneQ.OnSurvey.global.payment.port.out;

import OneQ.OnSurvey.global.payment.entity.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Optional<Payment> findByOrderId(String orderId);
    List<Payment> findPaymentsByUserKeyOrderByDate(Long userKey);
    Payment save(Payment payment);
    List<Payment> findPaidPaymentsByUserKey(long userKey);
}
