package OneQ.OnSurvey.global.auth.filter;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class SessionLoginRateLimiter {

    private final RateLimiterRegistry rateLimiterRegistry;
    private final RateLimiterConfig config;

    // IP당 1분에 5회 시도 제한
    private static final int LIMIT_FOR_PERIOD = 5;
    private static final Duration LIMIT_REFRESH_PERIOD = Duration.ofMinutes(5);
    private static final Duration TIMEOUT_DURATION = Duration.ZERO; // 즉시 실패

    public SessionLoginRateLimiter() {
        this.config = RateLimiterConfig.custom()
            .limitForPeriod(LIMIT_FOR_PERIOD)
            .limitRefreshPeriod(LIMIT_REFRESH_PERIOD)
            .timeoutDuration(TIMEOUT_DURATION)
            .build();
        this.rateLimiterRegistry = RateLimiterRegistry.of(config);
    }

    /**
     * 주어진 IP에 대해 요청을 허용할지 확인
     * @param clientIp 클라이언트 IP
     * @return true: 허용, false: 제한됨
     */
    public boolean tryConsume(String clientIp) {
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(clientIp, config);
        return rateLimiter.acquirePermission();
    }

    /**
     * 현재 사용 가능한 요청 횟수
     * @param clientIp 클라이언트 IP
     * @return 남은 요청 횟수
     */
    public int getAvailablePermissions(String clientIp) {
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(clientIp, config);
        return rateLimiter.getMetrics().getAvailablePermissions();
    }
}