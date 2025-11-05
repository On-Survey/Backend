package OneQ.OnSurvey.global.auth.token;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TokenStore {
    private final StringRedisTemplate redis;

    private String rtKey(Long userId) { return "rt:" + userId; }

    public void saveRefreshToken(Long userId, String refreshToken, Duration ttl) {
        redis.opsForValue().set(rtKey(userId), refreshToken, ttl);
    }

    public Optional<String> getRefreshToken(Long userId) {
        return Optional.of(redis.opsForValue().get(rtKey(userId)));
    }

    public void deleteRefresh(Long userId) {
        redis.delete(rtKey(userId));
    }

    // TODO 추후 RT 제거 필요
    public void saveValue(String key, String value, Duration ttl) {
        redis.opsForValue().set(key, value, ttl);
    }
    public Optional<String> getValue(String key) {
        return Optional.ofNullable(redis.opsForValue().get(key));
    }
    public void deleteKey(String key) { redis.delete(key); }
}
