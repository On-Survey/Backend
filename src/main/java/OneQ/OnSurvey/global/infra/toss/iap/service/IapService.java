package OneQ.OnSurvey.global.infra.toss.iap.service;

import OneQ.OnSurvey.domain.member.CoinErrorCode;
import OneQ.OnSurvey.domain.member.Member;
import OneQ.OnSurvey.domain.member.MemberErrorCode;
import OneQ.OnSurvey.domain.member.repository.MemberRepository;
import OneQ.OnSurvey.global.exception.CustomException;
import OneQ.OnSurvey.global.infra.toss.common.TossErrorCode;
import OneQ.OnSurvey.global.infra.toss.adapter.TossApiClient;
import OneQ.OnSurvey.global.infra.toss.iap.Payment;
import OneQ.OnSurvey.global.infra.toss.iap.PaymentStatus;
import OneQ.OnSurvey.global.infra.toss.iap.dto.OrderStatusResponse;
import OneQ.OnSurvey.global.infra.toss.iap.repository.PaymentRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.net.ssl.SSLContext;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class IapService {

    private final TossApiClient tossApiClient;
    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;

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

    /**
     * 토스 IAP 주문을 검증하고, 유효하면 우리 도메인에 결제 반영
     * @return 지급 성공 여부
     */
    @Transactional
    public boolean grantByOrder(long userKey, String orderId, Long surveyId, Long price) {

        if (price == null || price <= 0L) {
            throw new CustomException(CoinErrorCode.COIN_NOT_POSITIVE);
        }

        // 주문 상태 조회
        final OrderStatusResponse os;
        try {
            os = tossApiClient.getIapOrderStatus(tossSslContext, userKey, orderId);
        } catch (Exception e) {
            log.error("[IAP] getOrderStatus error, orderId={}", orderId, e);
            throw new CustomException(TossErrorCode.TOSS_IAP_GET_STATUS_ERROR);
        }

        final String status = os.status();
        if (status == null) {
            throw new CustomException(TossErrorCode.TOSS_IAP_INVALID_STATUS);
        }

        // 이미 결제 완료면 바로 성공
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        if (payment != null && payment.getStatus() == PaymentStatus.PAID) {
            return true;
        }

        // 최종 성공 상태만 허용
        if (!GRANTABLE.contains(status)) {
            log.warn("[IAP] not grantable status={}, orderId={}", status, orderId);
            throw new CustomException(TossErrorCode.TOSS_IAP_STATUS_NOT_GRANTED);
        }

        // 결제 레코드 생성/갱신
        if (payment == null) {
            payment = Payment.pending(
                    surveyId,
                    userKey,
                    os.orderId(),
                    os.sku(),
                    price.intValue()
            );
        }

        // PAID 마킹
        payment.markPaid(parseOsTime(os.statusDeterminedAt()));
        paymentRepository.save(payment);

        // 코인 지급
        Member member = memberRepository.findMemberByUserKey(userKey)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));
        member.increaseCoin(price);

        log.info("[IAP] coin granted: userKey={}, +{}(KRW==COIN), orderId={}",
                userKey, price, orderId);
        return true;
    }

    public OrderStatusResponse getStatus(long userKey, String orderId) {
        try {
            return tossApiClient.getIapOrderStatus(tossSslContext, userKey, orderId);
        } catch (Exception e) {
            log.error("[IAP] getStatus error, orderId={}", orderId, e);
            throw new CustomException(TossErrorCode.TOSS_IAP_GET_STATUS_ERROR);
        }
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
