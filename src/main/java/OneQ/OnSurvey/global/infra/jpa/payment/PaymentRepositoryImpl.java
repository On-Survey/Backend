package OneQ.OnSurvey.global.infra.jpa.payment;

import OneQ.OnSurvey.global.payment.entity.Payment;
import OneQ.OnSurvey.global.payment.port.out.PaymentRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static OneQ.OnSurvey.global.infra.toss.iap.QPayment.payment;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Payment> findByOrderId(String orderId) {
        return paymentJpaRepository.findByOrderId(orderId);
    }

    @Override
    public List<Payment> findPaymentsByUserKeyOrderByDate(Long userKey) {
        return queryFactory
                .selectFrom(payment)
                .where(payment.userKey.eq(userKey))
                .orderBy(
                        payment.paymentCompletedAt.desc(),
                        payment.id.desc()
                )
                .fetch();
    }

    @Override
    public void save(Payment payment) {
        paymentJpaRepository.save(payment);
    }
}
