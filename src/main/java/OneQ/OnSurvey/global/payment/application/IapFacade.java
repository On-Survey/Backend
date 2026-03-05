package OneQ.OnSurvey.global.payment.application;

import OneQ.OnSurvey.domain.member.CoinErrorCode;
import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.MemberErrorCode;
import OneQ.OnSurvey.domain.member.repository.MemberRepository;
import OneQ.OnSurvey.global.common.exception.CustomException;
import OneQ.OnSurvey.global.infra.discord.notifier.AlertNotifier;
import OneQ.OnSurvey.global.infra.discord.notifier.dto.PaymentCompletedAlert;
import OneQ.OnSurvey.global.infra.toss.client.TossApiClient;
import OneQ.OnSurvey.global.infra.toss.common.dto.iap.IapStatsResponse;
import OneQ.OnSurvey.global.infra.toss.common.dto.iap.OrderStatusResponse;
import OneQ.OnSurvey.global.infra.toss.common.exception.TossErrorCode;
import OneQ.OnSurvey.global.infra.transaction.AfterCommitExecutor;
import OneQ.OnSurvey.global.payment.PaymentErrorCode;
import OneQ.OnSurvey.global.payment.entity.Payment;
import OneQ.OnSurvey.global.payment.entity.PaymentPurpose;
import OneQ.OnSurvey.global.payment.entity.PaymentStatus;
import OneQ.OnSurvey.global.payment.port.out.PaymentRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.net.ssl.SSLContext;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class IapFacade implements IapUseCase {

    private final TossApiClient tossApiClient;
    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;

    private final AlertNotifier alertNotifier;
    private final AfterCommitExecutor afterCommitExecutor;

    @Value("${toss.secret.private-key}")
    private String privateKey;

    @Value("${toss.secret.public-crt}")
    private String publicCrt;

    private SSLContext tossSslContext;

    private static final Set<String> GRANTABLE = Set.of("PAYMENT_COMPLETED", "PURCHASED");

    @PostConstruct
    void initSsl() throws Exception {
        this.tossSslContext = tossApiClient.createSSLContext(publicCrt, privateKey);
    }

    @Override
    @Transactional
    public boolean grantByOrder(long userKey, String orderId, Long price) {

        log.info("[IAP] grantByOrder request: userKey={}, orderId={}, price={}",
                userKey, orderId, price);

        validatePricePositive(price);

        Payment payment = confirmAndSavePayment(userKey, orderId, price, PaymentPurpose.COIN_CHARGE);

        // 코인 지급
        Member member = memberRepository.findMemberByUserKey(userKey)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));
        member.increaseCoin(price);

        log.info("[IAP] coin granted: userKey={}, +{}(KRW==COIN), orderId={}",
                userKey, price, orderId);

        notifyAfterCommit(payment, userKey, orderId, price, PaymentPurpose.COIN_CHARGE, member.getCoin());

        return true;
    }

    @Override
    @Transactional
    public boolean grantHomeByOrder(long userKey, String orderId, Long price) {

        log.info("[IAP] grantHomeByOrder request: userKey={}, orderId={}, price={}", userKey, orderId, price);

        validatePricePositive(price);
        Payment payment = confirmAndSavePayment(userKey, orderId, price, PaymentPurpose.HOME_REGISTRATION);

        log.info("[IAP] home payment confirmed (no coin): userKey={}, orderId={}", userKey, orderId);

        notifyAfterCommit(payment, userKey, orderId, price, PaymentPurpose.HOME_REGISTRATION, -1L);

        return true;
    }

    @Override
    public OrderStatusResponse getStatus(long userKey, String orderId) {
        return fetchOrderStatus(userKey, orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public IapStatsResponse getMyIapStats(long userKey) {
        List<Payment> payments = paymentRepository.findPaidPaymentsByUserKey(userKey);

        long totalCount = payments.size();

        long totalAmount = 0L;
        for (Payment p : payments) {
            Integer amt = p.getTotalAmount();
            if (amt != null) totalAmount += amt.longValue();
        }

        return new IapStatsResponse(totalCount, totalAmount);
    }


    private Payment confirmAndSavePayment(long userKey, String orderId, Long price, PaymentPurpose purpose) {

        OrderStatusResponse os = fetchOrderStatus(userKey, orderId);

        String status = os.status();
        if (status == null) {
            throw new CustomException(TossErrorCode.TOSS_IAP_INVALID_STATUS);
        }

        // 이미 결제 완료면 멱등 처리
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        if (payment != null && payment.getStatus() == PaymentStatus.PAID) {

            // 목적 불일치 방어
            if (payment.getPurpose() != null && payment.getPurpose() != purpose) {
                throw new CustomException(PaymentErrorCode.PAYMENT_PURPOSE_MISMATCH);
            }
            return payment;
        }

        // 최종 성공 상태만 허용
        if (!GRANTABLE.contains(status)) {
            log.warn("[IAP] not grantable status={}, orderId={}", status, orderId);
            throw new CustomException(TossErrorCode.TOSS_IAP_STATUS_NOT_GRANTED);
        }

        LocalDateTime completedAt = parseOsTime(os.statusDeterminedAt());

        // 결제 레코드 생성/갱신
        if (payment == null) {
            payment = Payment.pending(
                    userKey,
                    os.orderId(),
                    os.sku(),
                    price != null ? price.intValue() : 0,
                    purpose
            );
        }

        payment.markPaid(completedAt);
        return paymentRepository.save(payment);
    }

    private OrderStatusResponse fetchOrderStatus(long userKey, String orderId) {
        try {
            return tossApiClient.getIapOrderStatus(tossSslContext, userKey, orderId);
        } catch (Exception e) {
            log.error("[IAP] getOrderStatus error, orderId={}", orderId, e);
            throw new CustomException(TossErrorCode.TOSS_IAP_GET_STATUS_ERROR);
        }
    }

    private void validatePricePositive(Long price) {
        if (price == null || price <= 0L) {
            throw new CustomException(CoinErrorCode.COIN_NOT_POSITIVE);
        }
    }

    private void notifyAfterCommit(
            Payment payment,
            long userKey,
            String orderId,
            Long price,
            PaymentPurpose paymentPurpose,
            Long coinAfter
    ) {
        String completedAt = String.valueOf(payment.getPaymentCompletedAt());

        PaymentCompletedAlert alert = new PaymentCompletedAlert(
                userKey,
                orderId,
                price,
                completedAt,
                paymentPurpose,
                coinAfter
        );

        afterCommitExecutor.run(() -> alertNotifier.sendPaymentCompletedAsync(alert));
    }

    private LocalDateTime parseOsTime(String iso) {
        try {
            return (iso == null || iso.isBlank())
                    ? LocalDateTime.now()
                    : OffsetDateTime.parse(iso).toLocalDateTime();
        } catch (Exception ignore) {
            return LocalDateTime.now();
        }
    }
}
