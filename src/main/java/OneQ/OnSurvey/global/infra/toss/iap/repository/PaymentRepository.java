package OneQ.OnSurvey.global.infra.toss.iap.repository;

import OneQ.OnSurvey.global.infra.toss.iap.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(String orderId);
}
