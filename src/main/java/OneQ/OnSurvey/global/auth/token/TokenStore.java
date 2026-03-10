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

    public void saveValue(String key, String value, Duration ttl) {
        redis.opsForValue().set(key, value, ttl);
    }
    public Optional<String> getValue(String key) {
        return Optional.ofNullable(redis.opsForValue().get(key));
    }
    public void deleteKey(String key) { redis.delete(key); }


    public boolean acquireLock(String key, Duration ttl) {
        Boolean ok = redis.opsForValue().setIfAbsent(key, "1", ttl);
        return Boolean.TRUE.equals(ok);
    }
    public void releaseLock(String key) {
        redis.delete(key);
    }
}
