package OneQ.OnSurvey.global.infra.toss.payment.jpa;

import OneQ.OnSurvey.global.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(String orderId);
}
